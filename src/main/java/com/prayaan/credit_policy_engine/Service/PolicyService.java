package com.prayaan.credit_policy_engine.Service;

import com.prayaan.credit_policy_engine.Entity.Rule;

import java.util.List;

public interface PolicyService {

    void reloadPolicy();

    List<Rule> getAllRules();

    Rule getRuleById(Integer ruleId);
}
