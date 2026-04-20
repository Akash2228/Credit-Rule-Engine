package com.prayaan.credit_policy_engine.Controller;

import com.prayaan.credit_policy_engine.Entity.Rule;
import com.prayaan.credit_policy_engine.Service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/policy")
@RequiredArgsConstructor
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    @PostMapping("/reload")
    public String reloadPolicy() {
        policyService.reloadPolicy();
        return "Policy reloaded successfully";
    }

    @GetMapping("/rules")
    public List<Rule> getRules(){
        return policyService.getAllRules();
    }
    
    @GetMapping("/rules/{ruleId}")
    public Rule getRule(@PathVariable(value = "ruleId") Integer ruleId) {
        return policyService.getRuleById(ruleId);
    }
}
