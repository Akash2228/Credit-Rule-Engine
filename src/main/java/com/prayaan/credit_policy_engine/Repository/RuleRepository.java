package com.prayaan.credit_policy_engine.Repository;

import com.prayaan.credit_policy_engine.Entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Integer> {

    List<Rule> findAllByIsActive(boolean isActive);

    Rule findByRuleIdAndIsActive(Integer ruleId,boolean isActive);
}
