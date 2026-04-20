package com.prayaan.credit_policy_engine.Dto;


import lombok.*;

@Getter
@Setter
@Builder
public class RuleResult {

    private Integer ruleId;
    private String ruleText;
    private String fieldChecked;

    private Object applicantValue;
    private String threshold;

    private String result;
    private String severity;
    private boolean passed;
    private String message;
}
