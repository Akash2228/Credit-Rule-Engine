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
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent";

    private final RestTemplate restTemplate = new RestTemplate();
    private final tools.jackson.databind.ObjectMapper objectMapper = new tools.jackson.databind.ObjectMapper();


    public List<Map<String, String>> parseBulkRuleExpressions(List<String> ruleExpressions) {
        try {
            String prompt = buildBulkPrompt(ruleExpressions);

            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> requestMap = Map.of("contents", List.of(content));

            String requestBody = objectMapper.writeValueAsString(requestMap);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String fullUrl = GEMINI_API_URL + "?key=" + API_KEY;

            System.out.println("Gemini Bulk Request: " + requestBody);

            String llmResponse = restTemplate.postForObject(fullUrl, entity, String.class);

            System.out.println("Gemini Raw Response: " + llmResponse);

            JsonNode rootNode = objectMapper.readTree(llmResponse);

            JsonNode candidates = rootNode.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new RuntimeException("No candidates in Gemini response");
            }

            JsonNode parts = candidates.get(0)
                    .path("content")
                    .path("parts");

            if (!parts.isArray() || parts.isEmpty()) {
                throw new RuntimeException("No parts in Gemini response");
            }

            String extractedJsonText = parts.get(0).path("text").asText();

            System.out.println("Extracted Raw Text: " + extractedJsonText);

            extractedJsonText = extractedJsonText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            System.out.println("Clean JSON Text: " + extractedJsonText);

            return objectMapper.readValue(
                    extractedJsonText,
                    new TypeReference<List<Map<String, String>>>() {}
            );

        } catch (Exception e) {
            throw new RuntimeException("Gemini BULK parsing failed", e);
        }
    }

    private String buildBulkPrompt(List<String> ruleExpressions) {

        String jsonStructureExample = """
    {
      "field": "credit_score",
      "operator": ">=",
      "threshold": "700",
      "condition": "loan_amount > 250000"
    }
    """;

        StringBuilder rulesText = new StringBuilder();
        for (int i = 0; i < ruleExpressions.size(); i++) {
            rulesText.append(i + 1)
                    .append(". ")
                    .append(ruleExpressions.get(i))
                    .append("\n");
        }

        return "You are a rule-parsing expert.\n" +
                "Convert ALL the following credit policy rules into a JSON ARRAY.\n\n" +

                "IMPORTANT RULES:\n" +
                "1. Each rule must be converted into JSON object\n" +
                "2. MAIN validation → field, operator, threshold\n" +
                "3. CONDITION → when rule applies\n" +
                "4. Do NOT repeat main rule inside condition\n" +
                "5. If no condition exists → condition = null\n\n" +

                "Example Output Object:\n" +
                jsonStructureExample + "\n\n" +

                "Rules:\n" +
                rulesText +

                "\nReturn ONLY JSON ARRAY like:\n" +
                "[{...},{...}]";
    }
}
