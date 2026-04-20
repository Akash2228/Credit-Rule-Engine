package com.prayaan.credit_policy_engine.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RULES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rule {

    @Id
    @Column(name = "RULE_ID")
    private Integer ruleId;

    @Column(name = "RULE_TEXT", length = 1000)
    private String ruleText;

    @Column(name = "FIELD")
    private String field;

    @Column(name = "OPERATOR")
    private String operator;

    @Column(name = "THRESHOLD")
    private String threshold;

    @Column(name = "CONDITION")
    private String condition;

    @Column(name = "SEVERITY")
    private String severity;

    @Column(name = "IS_ACTIVE")
    private boolean isActive = true;
}
