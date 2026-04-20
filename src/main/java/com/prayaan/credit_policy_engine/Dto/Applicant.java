package com.prayaan.credit_policy_engine.Dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Applicant {

    @Min(value = 18, message = "Applicant must be at least 18 years old")
    @Max(value = 99, message = "Applicant age cannot exceed 99 years")
    private int age;

    @Min(value = 0, message = "Monthly income cannot be negative")
    private double monthlyIncome;

    @NotBlank(message = "Employment type cannot be blank")
    private String employmentType;

    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 850, message = "Credit score cannot exceed 850")
    private int creditScore;

    @Min(value = 0, message = "Existing EMI obligations cannot be negative")
    private double existingEmiObligations;

    @Min(value = 1, message = "City tier must be at least 1")
    @Max(value = 3, message = "City tier cannot exceed 3")
    private int cityTier;
}
