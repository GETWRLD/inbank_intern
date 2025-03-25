package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.AgeConstants;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.InvalidAgeException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import ee.taltech.inbankbackend.exceptions.NoValidLoanException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@Service
public class DecisionEngine {

    // Used to check for the validity of the presented ID code.
    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private int creditModifier = 0;

    /**
     * Checks if the provided personal code is valid.
     * Extracted for better testability.
     */
    protected boolean isValidPersonalCode(String personalCode) {
        return validator.isValid(personalCode);
    }

    /**
     * Calculates the maximum loan amount and period for the customer based on their ID code,
     * the requested loan amount and the loan period.
     * The loan period must be between 12 and 48 months (inclusive).
     * The loan amount must be between 2000 and 10000€ months (inclusive).
     *
     * @param personalCode ID code of the customer that made the request.
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @return A Decision object containing the approved loan amount and period, and an error message (if any)
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * @throws NoValidLoanException If there is no valid loan found for the given ID code, loan amount and loan period
     * @throws InvalidAgeException If the customer doesn't meet age requirements
     */
    public Decision calculateApprovedLoan(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            NoValidLoanException, InvalidAgeException {
        // First check if the personal code is valid
        if (!isValidPersonalCode(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
        
        // Check age restrictions
        checkAgeRestrictions(personalCode, loanPeriod);
        
        // Get and set the credit modifier
        this.creditModifier = getCreditModifier(personalCode);
        
        // Check for debtor status before other validations
        if (this.creditModifier == 0) {
            throw new NoValidLoanException("No valid loan found!");
        }
        
        // Validate loan amount and period
        if (!(DecisionEngineConstants.MINIMUM_LOAN_AMOUNT <= loanAmount)
                || !(loanAmount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT)) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
        
        if (!(DecisionEngineConstants.MINIMUM_LOAN_PERIOD <= loanPeriod)
                || !(loanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD)) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }

        // Calculate the approvable loan amount and period
        int outputLoanAmount;
        int adjustedLoanPeriod = loanPeriod;
        
        // Safety guard against infinite loop - only check up to max period
        while (highestValidLoanAmount(adjustedLoanPeriod) < DecisionEngineConstants.MINIMUM_LOAN_AMOUNT 
               && adjustedLoanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            adjustedLoanPeriod++;
        }

        if (adjustedLoanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            outputLoanAmount = Math.min(DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT, 
                                        highestValidLoanAmount(adjustedLoanPeriod));
            return new Decision(outputLoanAmount, adjustedLoanPeriod, null);
        } else {
            throw new NoValidLoanException("No valid loan found!");
        }
    }

    /**
     * Checks if customer meets age requirements for a loan.
     * 
     * @param personalCode Customer's personal ID code
     * @param loanPeriod Requested loan period in months
     * @throws InvalidAgeException If customer is underage or too old for the loan
     * @throws InvalidPersonalCodeException If the personal code is invalid
     */
    private void checkAgeRestrictions(String personalCode, int loanPeriod) 
            throws InvalidAgeException, InvalidPersonalCodeException {
        // Extract age from personal code
        int age = getAgeFromPersonalCode(personalCode);
        
        // Check if customer is at least minimum age (18)
        if (age < AgeConstants.MINIMUM_AGE) {
            throw new InvalidAgeException("Customer must be at least " + AgeConstants.MINIMUM_AGE + 
                    " years old to apply for a loan.");
        }
        
        // Extract country code from personal code (for simplicity, first digit indicates country)
        String countryCode = getCountryCodeFromPersonalCode(personalCode);
        
        // Get expected lifetime for the country
        int expectedLifetime = AgeConstants.getExpectedLifetime(countryCode);
        
        // Calculate maximum allowed age (expected lifetime minus loan period in years)
        int loanPeriodInYears = (int) Math.ceil(loanPeriod / 12.0);
        int maxAge = expectedLifetime - loanPeriodInYears;
        
        // Check if customer is not too old
        if (age > maxAge) {
            throw new InvalidAgeException("Based on the expected lifetime in your country, " +
                    "we cannot offer loans with this period to customers over " + maxAge + " years old.");
        }
    }
    
    /**
     * Extracts the age of a person from their personal code.
     * 
     * @param personalCode The personal code
     * @return The age in years
     * @throws InvalidPersonalCodeException If the personal code is invalid
     */
    // Changed from private to protected for testing purposes
    protected int getAgeFromPersonalCode(String personalCode) throws InvalidPersonalCodeException {
        try {
            // Extract birth date from Estonian personal code
            int century = (personalCode.charAt(0) <= '2') ? 1800 : 
                         ((personalCode.charAt(0) <= '4') ? 1900 : 2000);
            int year = century + Integer.parseInt(personalCode.substring(1, 3));
            int month = Integer.parseInt(personalCode.substring(3, 5));
            int day = Integer.parseInt(personalCode.substring(5, 7));
            
            LocalDate birthDate = LocalDate.of(year, month, day);
            LocalDate currentDate = LocalDate.now();
            
            return Period.between(birthDate, currentDate).getYears();
        } catch (Exception e) {
            throw new InvalidPersonalCodeException("Could not parse birth date from personal code", e);
        }
    }
    
    /**
     * Extracts the country code from a personal code.
     * For simplicity, we use the first digit:
     * 1-2: Estonia (EE)
     * 3-4: Latvia (LV)
     * 5-6: Lithuania (LT)
     * 
     * @param personalCode The personal code
     * @return Two-letter country code
     */
    // Changed from private to protected for testing purposes
    protected String getCountryCodeFromPersonalCode(String personalCode) {
        char firstDigit = personalCode.charAt(0);
        
        if (firstDigit == '1' || firstDigit == '2') {
            return "EE"; // Estonia
        } else if (firstDigit == '3' || firstDigit == '4') {
            return "LV"; // Latvia
        } else {
            return "LT"; // Lithuania (or others)
        }
    }

    /**
     * Calculates the largest valid loan for the current credit modifier and loan period.
     */
    private int highestValidLoanAmount(int loanPeriod) {
        return creditModifier * loanPeriod;
    }

    /**
     * Calculates the credit modifier of the customer according to the last four digits of their ID code.
     * Extracted for better testability.
     */
    protected int getCreditModifier(String personalCode) {
        int segment = Integer.parseInt(personalCode.substring(personalCode.length() - 4));

        if (segment < 2500) {
            return 0;
        } else if (segment < 5000) {
            return DecisionEngineConstants.SEGMENT_1_CREDIT_MODIFIER;
        } else if (segment < 7500) {
            return DecisionEngineConstants.SEGMENT_2_CREDIT_MODIFIER;
        }

        return DecisionEngineConstants.SEGMENT_3_CREDIT_MODIFIER;
    }

    /**
     * Verify that all inputs are valid according to business rules.
     * If inputs are invalid, then throws corresponding exceptions.
     *
     * @param personalCode Provided personal ID code
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     */
    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException {

        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_AMOUNT <= loanAmount)
                || !(loanAmount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT)) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_PERIOD <= loanPeriod)
                || !(loanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD)) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }

    }
}
