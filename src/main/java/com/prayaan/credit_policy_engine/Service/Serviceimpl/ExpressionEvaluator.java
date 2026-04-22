package com.prayaan.credit_policy_engine.Service.Serviceimpl;

import com.prayaan.credit_policy_engine.Dto.LoanApplicationRequest;
import com.prayaan.credit_policy_engine.Dto.RuleResult;
import com.prayaan.credit_policy_engine.Entity.Rule;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
public class ExpressionEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();


    public RuleResult evaluateRule(Rule rule, LoanApplicationRequest request) {

        boolean isPassed = false;
        String message = "";
        Object applicantValue = null;

        try {
            if ("LOW".equalsIgnoreCase(rule.getSeverity())) {
                return RuleResult.builder()
                        .ruleId(rule.getRuleId())
                        .ruleText(rule.getRuleText())
                        .passed(true)
                        .severity(rule.getSeverity())
                        .message("Informational rule (no validation)")
                        .build();
            }

            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("applicant", request.getApplicant());
            context.setVariable("loanRequest", request.getLoanRequest());

            boolean conditionSatisfied = true;

            if (rule.getCondition() != null && !rule.getCondition().trim().isEmpty()) {

                String conditionExpr = mapConditionPaths(rule.getCondition());

                conditionSatisfied = Boolean.TRUE.equals(
                        parser.parseExpression(conditionExpr).getValue(context, Boolean.class)
                );
            }

            if (!conditionSatisfied) {
                return RuleResult.builder()
                        .ruleId(rule.getRuleId())
                        .ruleText(rule.getRuleText())
                        .fieldChecked(rule.getField())
                        .applicantValue(null)
                        .threshold(rule.getThreshold())
                        .passed(true)
                        .severity(rule.getSeverity())
                        .message("Rule skipped (condition not applicable)")
                        .build();
            }

            String fieldExpression = mapToSpelPath(rule.getField());
            Object valueObj = parser.parseExpression(fieldExpression).getValue(context);

            if (valueObj == null) {
                throw new RuntimeException("Field value is null");
            }

            applicantValue = valueObj;
            double value = Double.parseDouble(valueObj.toString());

            String operator = rule.getOperator();
            String threshold = rule.getThreshold();

            if (threshold != null && threshold.contains("%")) {
                double val = Double.parseDouble(threshold.replace("%", "").trim());
                threshold = String.valueOf(val / 100);
            }

            if ("BETWEEN".equalsIgnoreCase(operator)) {

                String[] parts = threshold.split("-");
                double lower = Double.parseDouble(parts[0]);
                double upper = Double.parseDouble(parts[1]);

                isPassed = value >= lower && value <= upper;

            } else {

                String expression = fieldExpression + " " + operator + " " + threshold;

                isPassed = Boolean.TRUE.equals(
                        parser.parseExpression(expression).getValue(context, Boolean.class)
                );
            }

            message = isPassed ? "Rule Passed" : "Rule Failed";

        } catch (Exception e) {
            message = "Error evaluating rule '" + rule.getRuleId() + "': " + e.getMessage();
        }

        return RuleResult.builder()
                .ruleId(rule.getRuleId())
                .ruleText(rule.getRuleText())
                .fieldChecked(rule.getField())
                .applicantValue(applicantValue)
                .threshold(rule.getThreshold())
                .passed(isPassed)
                .severity(rule.getSeverity())
                .message(message)
                .build();
    }

    private String mapToSpelPath(String fieldName) {
        return switch (fieldName.toLowerCase()) {
            case "credit_score" -> "#applicant.creditScore";
            case "age" -> "#applicant.age";
            case "monthly_income" -> "#applicant.monthlyIncome";
            case "existing_emi_obligations" -> "#applicant.existingEmiObligations";
            case "city_tier" -> "#applicant.cityTier";
            case "loan_amount" -> "#loanRequest.amount";
            case "tenure_months" -> "#loanRequest.tenureMonths";
            case "foir" -> "(#applicant.existingEmiObligations / #applicant.monthlyIncome)";
            default -> fieldName;
        };
    }

    private String mapConditionPaths(String condition) {
        return condition
                .replace("employment_type", "#applicant.employmentType")
                .replace("city_tier", "#applicant.cityTier")
                .replace("credit_score", "#applicant.creditScore")
                .replace("loan_amount", "#loanRequest.amount")
                .replace("age_at_loan_maturity", "#applicant.age")
                .replace("foir", "(#applicant.existingEmiObligations / #applicant.monthlyIncome)");
    }
}
