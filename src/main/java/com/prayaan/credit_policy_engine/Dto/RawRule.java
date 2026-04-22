package com.prayaan.credit_policy_engine.Dto;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Data
@Getter
@Setter
public class RawRule {
    public Integer ruleId;
    public String severity;
    public String ruleText;
    public String ruleExpression;

    public RawRule(Integer ruleId, String severity, String line, String ruleExpression) {
        this.ruleId = ruleId;
        this.severity = severity;
        this.ruleText = line;
        this.ruleExpression = ruleExpression;
    }
}
