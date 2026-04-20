package com.prayaan.credit_policy_engine.Service.Serviceimpl;

import com.prayaan.credit_policy_engine.Entity.Rule;
import com.prayaan.credit_policy_engine.Repository.RuleRepository;
import com.prayaan.credit_policy_engine.Service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PolicyServiceImpl implements PolicyService {


    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private  PolicyParser policyParser;


    @Override
    @Transactional
    public void reloadPolicy() {

        List<Rule> newRules;

        try {
            newRules = policyParser.parsePolicy();

            if (newRules == null || newRules.isEmpty()) {
                throw new RuntimeException("Parsed rules are empty");
            }

        } catch (Exception e) {
            System.err.println("Policy reload failed. Keeping existing rules active.");
            throw new RuntimeException("Policy reload aborted", e);
        }

        List<Rule> existingRules = ruleRepository.findAllByIsActive(true);
        existingRules.forEach(rule -> rule.setActive(false));
        ruleRepository.saveAll(existingRules);

        newRules.forEach(rule -> rule.setActive(true));
        ruleRepository.saveAll(newRules);
    }

    @Override
    public List<Rule> getAllRules() {
        return ruleRepository.findAllByIsActive(true);
    }

    @Override
    public Rule getRuleById(Integer ruleId) {
        return ruleRepository.findByRuleIdAndIsActive(ruleId,true);
    }
}
