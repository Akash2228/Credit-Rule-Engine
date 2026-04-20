package com.prayaan.credit_policy_engine.Dto;

import com.prayaan.credit_policy_engine.Enum.RuleDecision;
import lombok.*;
import java.util.*;

@Getter
@Setter
@Builder
public class EvaluationResponse {

    private String applicationId;
    private RuleDecision decision;
    private double confidence;

    private List<RuleResult> rulesEvaluated;

    private String summary;
    private List<String> failedRuleIds;
}
