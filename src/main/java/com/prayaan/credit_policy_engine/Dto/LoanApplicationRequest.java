package com.prayaan.credit_policy_engine.Dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequest {

    @NotBlank(message = "Application ID cannot be blank")
    private String applicationId;

    @NotNull(message = "Applicant details cannot be null")
    @Valid
    private Applicant applicant;

    @NotNull(message = "Loan request details cannot be null")
    @Valid // Enables validation on the nested LoanRequest object
    private LoanRequest loanRequest;
}
