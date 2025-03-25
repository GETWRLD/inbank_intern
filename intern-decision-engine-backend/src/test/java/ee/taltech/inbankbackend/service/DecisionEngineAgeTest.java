package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.exceptions.InvalidAgeException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DecisionEngineAgeTest {

    // Test with a manually created spy rather than using @InjectMocks
    @Test
    void testUnderageCustomer() throws InvalidPersonalCodeException {
        // Create a spy of DecisionEngine
        DecisionEngine spyEngine = spy(new DecisionEngine());
        
        // Mock the validator to always return true for validation
        doReturn(true).when(spyEngine).isValidPersonalCode(anyString());
        
        // Mock the age check to return 17 (underage)
        doReturn(17).when(spyEngine).getAgeFromPersonalCode(anyString());
        
        // Should throw InvalidAgeException for customers under 18
        assertThrows(InvalidAgeException.class,
                () -> spyEngine.calculateApprovedLoan("50705073711", 5000L, 24));
    }

    @Test
    void testSeniorEstonianCustomer() throws InvalidPersonalCodeException {
        // Create a spy of DecisionEngine
        DecisionEngine spyEngine = spy(new DecisionEngine());
        
        // Mock the validator to always return true for validation
        doReturn(true).when(spyEngine).isValidPersonalCode(anyString());
        
        // Removed unnecessary stubbing for getCreditModifier
        
        // Override methods to simulate Estonian senior
        doReturn(77).when(spyEngine).getAgeFromPersonalCode(anyString());
        doReturn("EE").when(spyEngine).getCountryCodeFromPersonalCode(anyString());
        
        // Should throw InvalidAgeException for Estonian customer age 77 with 24-month loan
        assertThrows(InvalidAgeException.class,
                () -> spyEngine.calculateApprovedLoan("34605073722", 5000L, 24));
    }

    @Test
    void testSeniorLatvianCustomer() throws InvalidPersonalCodeException {
        // Create a spy of DecisionEngine
        DecisionEngine spyEngine = spy(new DecisionEngine());
        
        // Mock the validator to always return true for validation
        doReturn(true).when(spyEngine).isValidPersonalCode(anyString());
        
        // Removed unnecessary stubbing for getCreditModifier
        
        // Override methods to simulate Latvian senior
        doReturn(74).when(spyEngine).getAgeFromPersonalCode(anyString());
        doReturn("LV").when(spyEngine).getCountryCodeFromPersonalCode(anyString());
        
        // Should throw InvalidAgeException for Latvian customer age 74 with 24-month loan
        assertThrows(InvalidAgeException.class,
                () -> spyEngine.calculateApprovedLoan("35505073733", 5000L, 24));
    }

    @Test
    void testSeniorLithuanianCustomer() throws InvalidPersonalCodeException {
        // Create a spy of DecisionEngine
        DecisionEngine spyEngine = spy(new DecisionEngine());
        
        // Mock the validator to always return true for validation
        doReturn(true).when(spyEngine).isValidPersonalCode(anyString());
        
        // Removed unnecessary stubbing for getCreditModifier
        
        // Override methods to simulate Lithuanian senior
        doReturn(75).when(spyEngine).getAgeFromPersonalCode(anyString());
        doReturn("LT").when(spyEngine).getCountryCodeFromPersonalCode(anyString());
        
        // Should throw InvalidAgeException for Lithuanian customer age 75 with 24-month loan
        assertThrows(InvalidAgeException.class,
                () -> spyEngine.calculateApprovedLoan("55505073744", 5000L, 24));
    }
}
