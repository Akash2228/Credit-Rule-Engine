# Credit Policy Rule Engine

A dynamic, scalable rule engine for evaluating credit policies using structured rules parsed from human-readable policy documents.

##  Overview

This system enables:
- Dynamic rule ingestion from policy files
- Batch parsing using LLM (Gemini)
- Structured rule storage in DB
- Deterministic rule evaluation (no LLM at runtime)
- Explainable decision making

##  Key Features

-  Batch rule parsing using Gemini (reduces cost & latency)
-  Atomic policy reload (no partial updates)
-  Dynamic rule configuration (no code changes required)
-  Explainable evaluation (rule-level results)
-  Separation of parsing and evaluation
-  Scalable and stateless design

##  APIs

# GET /rules 
Returns all active rules 

# GET /rules/{id}  
Fetch active specific rule

# POST /policy/reload 

Reads rules from policy file
Uses Gemini (batch processing) to parse all rules at once
Converts them into structured format
Validates all rules

# POST /evaluate

Request
{
  "applicationId": "APP-0102",
  "applicant": {
    "age": 34,
    "monthlyIncome": 300000,
    "creditScore": 700,
    "existingEmiObligations": 20000,
    "employmentType": "salaried",
    "cityTier": 1
  },
  "loanRequest": {
    "amount": 3000000,
    "tenureMonths": 36
  }
}

Response :

Rule-wise evaluation
Failed rules
Final decision
Confidence score

# Policy Reload Flow

Policy File → Gemini (Batch Parsing) → Normalize → Validate → Activate Rules

# Evaluation Flow

Request → Fetch Active Rules → Evaluate → Aggregate → Decision

