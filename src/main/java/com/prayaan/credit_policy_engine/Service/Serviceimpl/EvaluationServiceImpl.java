package com.prayaan.credit_policy_engine.Service.Serviceimpl;

import com.prayaan.credit_policy_engine.Dto.EvaluationResponse;
import com.prayaan.credit_policy_engine.Dto.LoanApplicationRequest;
import com.prayaan.credit_policy_engine.Dto.RuleResult;
import com.prayaan.credit_policy_engine.Entity.Rule;
import com.prayaan.credit_policy_engine.Enum.RuleDecision;
import com.prayaan.credit_policy_engine.Repository.RuleRepository;
import com.prayaan.credit_policy_engine.Service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private  ExpressionEvaluator expressionEvaluator ;

    @Override
    public EvaluationResponse evaluate(LoanApplicationRequest request) {
        List<Rule> activeRules = ruleRepository.findAllByIsActive(true);

        List<RuleResult> ruleResults = activeRules.stream()
                .map(rule -> expressionEvaluator.evaluateRule(rule, request))
                .collect(Collectors.toList());

        RuleDecision finalDecision = calculateFinalDecision(ruleResults);

        List<String> failedRuleIds = ruleResults.stream()
                .filter(r -> !r.isPassed())
                .map(r -> String.valueOf(r.getRuleId()))
                .collect(Collectors.toList());

        double confidence = (double) ruleResults.stream()
                .filter(RuleResult::isPassed)
                .count() / ruleResults.size();

        String summary;

        long failedCount = ruleResults.stream().filter(r -> !r.isPassed()).count();

        if (failedCount == 0) {
            summary = "All rules passed. Application approved.";
        } else {
            summary = "Application failed " + failedCount + " rule(s). Decision: " + finalDecision;
        }

        return EvaluationResponse.builder()
                .applicationId(request.getApplicationId())
                .decision(finalDecision)
                .rulesEvaluated(ruleResults)
                .failedRuleIds(failedRuleIds)
                .confidence(confidence)
                .summary(summary)
                .build();
    }

    private RuleDecision calculateFinalDecision(List<RuleResult> ruleResults) {

        boolean hasHighFail = false;
        boolean hasMediumFail = false;

        for (RuleResult r : ruleResults) {
            if (!r.isPassed()) {
                if ("HIGH".equalsIgnoreCase(r.getSeverity())) {
                    hasHighFail = true;
                } else if ("MEDIUM".equalsIgnoreCase(r.getSeverity())) {
                    hasMediumFail = true;
                }
            }
        }

        if (hasHighFail) return RuleDecision.REJECTED;
        if (hasMediumFail) return RuleDecision.NEEDS_REVIEW;
        return RuleDecision.APPROVED;
    }
}
