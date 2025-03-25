package ee.taltech.inbankbackend.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds constants related to age restrictions for loan decision-making.
 */
public class AgeConstants {
    public static final int MINIMUM_AGE = 18;
    
    // Expected lifetimes for Baltic countries (in years)
    private static final Map<String, Integer> COUNTRY_EXPECTED_LIFETIMES = new HashMap<>();
    
    static {
        // Average life expectancies in Baltic countries (source: arbitrary as per requirements)
        COUNTRY_EXPECTED_LIFETIMES.put("EE", 78); // Estonia
        COUNTRY_EXPECTED_LIFETIMES.put("LV", 75); // Latvia
        COUNTRY_EXPECTED_LIFETIMES.put("LT", 76); // Lithuania
    }
    
    /**
     * Get the expected lifetime for a given country code.
     * 
     * @param countryCode The two-letter country code
     * @return The expected lifetime or 75 as default if country not found
     */
    public static int getExpectedLifetime(String countryCode) {
        return COUNTRY_EXPECTED_LIFETIMES.getOrDefault(countryCode, 75);
    }
}
