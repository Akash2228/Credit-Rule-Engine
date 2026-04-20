package com.prayaan.credit_policy_engine.Service.llm;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";


    private final RestTemplate restTemplate = new RestTemplate();
    private final tools.jackson.databind.ObjectMapper objectMapper = new tools.jackson.databind.ObjectMapper();


    public Map<String, String> parseRuleExpression(String ruleExpression) {
        try {
            String prompt = buildPromptForExpression(ruleExpression);

            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> requestMap = Map.of("contents", List.of(content));

            String requestBody = objectMapper.writeValueAsString(requestMap);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String fullUrl = GEMINI_API_URL + "?key=" + API_KEY;

            System.out.println("Gemini Request: " + requestBody);

            String llmResponse = restTemplate.postForObject(fullUrl, entity, String.class);

            System.out.println("Gemini Raw Response: " + llmResponse);

            JsonNode rootNode;
            try {
                rootNode = objectMapper.readTree(llmResponse);
            } catch (Exception e) {
                throw new RuntimeException("Invalid JSON from Gemini: " + llmResponse, e);
            }

            JsonNode candidates = rootNode.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new RuntimeException("No candidates in Gemini response: " + llmResponse);
            }

            JsonNode parts = candidates.get(0)
                    .path("content")
                    .path("parts");

            if (!parts.isArray() || parts.isEmpty()) {
                throw new RuntimeException("No parts in Gemini response: " + llmResponse);
            }

            String extractedJsonText = parts.get(0).path("text").asText();

            System.out.println("🧠 Extracted Raw Text: " + extractedJsonText);

            extractedJsonText = extractedJsonText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            System.out.println("🧠 Clean JSON Text: " + extractedJsonText);
            try {
                return objectMapper.readValue(
                        extractedJsonText,
                        new TypeReference<Map<String, String>>() {}
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse extracted JSON: " + extractedJsonText, e);
            }

        } catch (Exception e) {
            System.err.println("Gemini parsing failed for: " + ruleExpression);
            e.printStackTrace();
            throw new RuntimeException("Gemini API parsing failed for expression: " + ruleExpression, e);
        }
    }

    private String buildPromptForExpression(String ruleExpression) {

        String jsonStructureExample = """
    {
      "field": "credit_score",
      "operator": ">=",
      "threshold": "700",
      "condition": "loan_amount > 250000"
    }
    """;

        return "You are a rule-parsing expert.\n" +
                "Convert the given credit policy rule into structured JSON.\n\n" +

                "IMPORTANT RULES:\n" +
                "1. The MAIN validation must go into 'field', 'operator', 'threshold'\n" +
                "2. The CONDITION (when rule applies) must go into 'condition'\n" +
                "3. CONDITION should NOT repeat the main rule\n" +
                "4. If no condition exists, set condition = null\n\n" +

                "Example:\n" +
                "Input: 'Applicants with loan amount > 250000 must have credit score >= 700'\n" +
                "Output: " + jsonStructureExample + "\n\n" +

                "Rule Expression: '" + ruleExpression + "'\n" +
                "Return ONLY JSON. No explanation.";
    }
}
