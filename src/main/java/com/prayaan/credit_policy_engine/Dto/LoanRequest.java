package com.prayaan.credit_policy_engine.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequest {

    private double amount;
    private int tenureMonths;
    private String purpose;
}
