package com.prayaan.credit_policy_engine.Service;

import com.prayaan.credit_policy_engine.Dto.EvaluationResponse;
import com.prayaan.credit_policy_engine.Dto.LoanApplicationRequest;

public interface EvaluationService {

    EvaluationResponse evaluate(LoanApplicationRequest request);
}
