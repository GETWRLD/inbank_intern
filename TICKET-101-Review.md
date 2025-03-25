# TICKET-101 Code Review: Decision Engine MVP

## Overview of the code

This is an overview of a TICKET-101 code implementation. The main task was to create a decision engine which takes in personal code, loan amount, loan period in months and returns a decision (negative or positive) and the amount.

## CRITICAL SHORTCOMINGS

### 1. Incorrect Maximum Loan Period

The assignment clearly states, that the maximum loan period should be 48 months, however maximum loan period of 60 months was configured both in the backend constants as well as on the frontend slider.

This is a serious violation of instructions which may potentially have critical consequences for the company, since the system could offer loan terms which do not meet bank's policy.

Since this is a critical issue, it is strongly reccommended to immediately change the maximum loan period to 48 months in both frontend and backend code.

### 2. Incorrect Minimum Loan Period

Instrucions stated, that minimum loan period should be 12 months. Despite that, on the frontend side, a minimum loan period of 6 months is displayed. On the backend side, however the minimum loan period of 12 months is configured correctly.

This is an issue that requires patching as soon as possible. Even though, this mistake most likely will not result in an incorrect loan decision, since the backend calculations are performed correctly, it significantly reduces user friendliness of an application, confusing customers.

### 3. Incorrect Maximum Loan Size

The decision engine is supposed to determine what would be the maximum sum, regardless of the person requested loan amount. As stated in the assignment: "For example if a person applies for €4000,-, but we determine that we would approve a larger sum then the result should be the maximum sum which we would approve." Current version of the decision engine fails to meet this requirement and simply gives maximim amount requested by the client as a result of a loan calculation or, in case client does not have high enough credit score, shows maximum loan size that is less than the requested amount.

This is a serious fault in the decision engine. It may lead to miscalculations and cause potential financial losses for the company. It is neccessary to immediately address this issue and update the engine.

## Requirements Verification

### Requirements that were met
1. Takes personal code, loan amount, and loan period as inputs
2. Returns negative or positive loan decision and approved loan size
3. Handles the 4 required scenarios (debt and 3 different segments)
4. Implements the credit scoring algorithm correctly
5. Respects loan amount constraints (2000€ - 10000€)
6. Allows adjusting loan period for finding suitable loan size

### Requirements that were not met
1. Does not respect loan period constraints. Assignment states, that the loan period should be between 12 and 48 months, whereas current version of the decision engine defines maximum loan period as 60 months. In addition to that, on the frontend part it is written that the minimum loan period is 6 months, when according to the assignment it is supposed to be 12 months. However, in the backend code, minimum loan period is defined correctly.
2. Fails to find maximum approvable amount. Decision engine shows either maximum possible amount, which is less than the requested amount if the credit score of the client is not high enough, or simply the amount that the client requested, but does not show theoretically maximum loan size that could be approved for this client.


## Strengths

1. **Logic**: The decision engine correctly determines maximum approvable amounts
2. **Edge Cases**: The code handles the debt scenario properly, rejecting all loan applications for individuals with debt

## Areas for Improvement (SOLID Principles)

#### Single Responsibility Principle
1. The decision engine currently handles too many responsibilities (validation, scoring, decision-making)
2. It is worth considering to separate the system into distinct classes:
  "InputValidator" - for validating personal codes and loan parameters
  "CreditScorer" - for calculating credit scores
  "DecisionEngine" - for making final decisions based on scores

#### Open/Closed Principle
1. Current implementation with hardcoded values, credit scores and credit modifiers is not easily extensible
2. It is worth implementing a strategy pattern for different scoring algorithms
3. Configuration files or database for credit modifiers instead of hardcoding would be more practical

#### Liskov Substitution Principle
- Creation of proper interfaces and implementations that can be substituted without breaking functionality

#### Interface Segregation Principle
- Defining focused interfaces (e.g., "IScorer", "IDecisionMaker") instead of one large interface

#### Dependency Inversion Principle
- High-level modules should depend on abstractions, not concrete implementations

### Code Structure
- Moving hardcoded values to configuration
- Improving error handling with specific exception types
- Adding comprehensive logging
- Implementing proper unit testing

## Conclusion

The TICKET-101 code implementation partially meets the functional requirements and does not deliver a working product due to significant flaws in the source code at this stage. Despite the fact, that the intern demonstrated good understanding of the business logic and algorithm implementation, some software design principles were breached and a substantial amount of technical tasks were not fulfilled. However, current version of the project makes a good foundation for further improvements and fixes. With the suggested improvements, successful delivery of working product is highly possible in the future.