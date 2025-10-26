package util;

import java.util.Random;

/**
 * Utility class for generating and verifying time-based OTPs
 * Uses current time as seed for synchronized OTP generation
 * 
 * @author TAMIL MUGHILAN
 */
public class OTPUtil {
    
    //Using timestamp as a seed for generating synchronized OTP
	/**
     * Generates a 6-digit OTP using current time as seed.
     * Uses minutes to ensure same OTP within same minute.
     *
     * @return 6-digit OTP as String
     */
    public static String generateOTP() {
       
        long currentTimeMinutes = System.currentTimeMillis() / (1000 * 60);
        Random random = new Random(currentTimeMinutes);
        
        //6 digit number
        int otp = random.nextInt(900000) + 100000;
        
        System.out.println("Generated OTP: " + otp + " (Seed: " + currentTimeMinutes + ")");
        return String.valueOf(otp);
    }
    
   //Method for verifying OTP
    /**
     * Verifies if the entered OTP matches the expected OTP.
     * Generates expected OTP using current time and compares.
     *
     * @param enteredOTP the OTP entered by user
     * @return true if OTP is valid, false otherwise
     */
    public static boolean verifyOTP(String enteredOTP) {
        String expectedOTP = generateOTP();
        boolean isValid = expectedOTP.equals(enteredOTP);
        
        System.out.println("Expected OTP: " + expectedOTP);
        System.out.println("Entered OTP: " + enteredOTP);
        System.out.println("OTP Valid: " + isValid);
        
        return isValid;
    }
    
    // For testing - generate OTP with custom seed
    /**
     * Generates OTP with custom seed for testing purposes.
     * Useful for testing with known seeds.
     *
     * @param seed custom seed for random number generation
     * @return 6-digit OTP as String
     */
    public static String generateOTPWithSeed(long seed) {
        Random random = new Random(seed);
        int otp = random.nextInt(900000) + 100000;
        System.out.println("Generated OTP with seed " + seed + ": " + otp);
        return String.valueOf(otp);
    }
}