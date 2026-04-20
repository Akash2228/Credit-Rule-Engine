package com.prayaan.credit_policy_engine.Controller;

import com.prayaan.credit_policy_engine.Dto.EvaluationResponse;
import com.prayaan.credit_policy_engine.Dto.LoanApplicationRequest;
import com.prayaan.credit_policy_engine.Service.EvaluationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evaluate")
public class EvaluationController {

    @Autowired
    private  EvaluationService evaluationService;

    @PostMapping
    public EvaluationResponse evaluate(@RequestBody @Valid LoanApplicationRequest request) {
        return evaluationService.evaluate(request);
    }
}
