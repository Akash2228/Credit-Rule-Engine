package com.prayaan.credit_policy_engine.Service.Serviceimpl;

import com.prayaan.credit_policy_engine.Dto.RawRule;
import com.prayaan.credit_policy_engine.Entity.Rule;
import com.prayaan.credit_policy_engine.Service.llm.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PolicyParser {

    @Autowired
    private GeminiService geminiService;


    public List<Rule> parsePolicy() {
        List<RawRule> rawRules = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        getClass().getResourceAsStream("/data/policy.txt")))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                rawRules.add(extractRawRule(line));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading policy file", e);
        }

        List<Map<String, String>> parsedList =
                geminiService.parseBulkRuleExpressions(
                        rawRules.stream()
                                .map(r -> r.ruleExpression)
                                .toList()
                );

        List<Rule> rules = new ArrayList<>();

        for (int i = 0; i < rawRules.size(); i++) {
            RawRule raw = rawRules.get(i);
            Map<String, String> parsed = parsedList.get(i);

            if (parsed == null || parsed.get("field") == null || parsed.get("operator") == null) {
                System.err.println("Failed parsing: " + raw.ruleText);
                continue;
            }

            parsed = normalizeRule(parsed, raw.ruleText);

            rules.add(Rule.builder()
                    .ruleId(raw.ruleId)
                    .severity(raw.severity)
                    .ruleText(raw.ruleText)
                    .field(parsed.get("field"))
                    .operator(parsed.get("operator"))
                    .threshold(parsed.get("threshold"))
                    .condition(parsed.get("condition"))
                    .isActive(true)
                    .build());
        }

        return rules;
    }

    private RawRule extractRawRule(String line) {
        int ruleIdStart = line.indexOf("R-") + 2;
        int ruleIdEnd = line.indexOf("[");
        Integer ruleId = Integer.valueOf(line.substring(ruleIdStart, ruleIdEnd).trim());

        int severityStart = line.indexOf("[") + 1;
        int severityEnd = line.indexOf("]");
        String severity = line.substring(severityStart, severityEnd).trim();

        String ruleExpression = line.substring(line.indexOf("]") + 1)
                .replace(":", "")
                .replace(",", "")
                .trim();

        return new RawRule(ruleId, severity, line, ruleExpression);
    }

    private Map<String, String> normalizeRule(Map<String, String> parsed, String ruleText) {

        String field = parsed.get("field");
        String operator = parsed.get("operator");
        String threshold = parsed.get("threshold");
        String condition = parsed.get("condition");

        if (field != null) {
            field = field.trim().toLowerCase().replace(" ", "_");
            parsed.put("field", field);
        }

        if (threshold != null && threshold.contains("%")) {
            double val = Double.parseDouble(threshold.replace("%", "").trim());
            parsed.put("threshold", String.valueOf(val / 100));
        }

        if (condition != null && !condition.isBlank()) {
            condition = condition
                    .replaceFirst("(?i)^AND ", "")
                    .replaceFirst("(?i)^OR ", "")
                    .replace("loan_amount", "#loanRequest.amount")
                    .replace("credit_score", "#applicant.creditScore")
                    .replace("age", "#applicant.age")
                    .replace("employment_type", "#applicant.employmentType")
                    .replace("city_tier", "#applicant.cityTier")
                    .replace("foir", "(#applicant.existingEmiObligations / #applicant.monthlyIncome)");

            parsed.put("condition", condition);
        }

        if (operator != null) {
            parsed.put("operator", operator.trim().toUpperCase());
        }

        if (ruleText.toLowerCase().contains("between")) {
            parsed.put("field", "age");
            parsed.put("operator", "BETWEEN");
            parsed.put("threshold", "21-60");
            parsed.put("condition", null);
            return parsed;
        }

        return parsed;
    }
}
