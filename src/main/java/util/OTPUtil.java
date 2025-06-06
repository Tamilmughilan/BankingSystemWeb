package util;

import java.util.Random;

public class OTPUtil {
    
    //Using timestamp as a seed for generating synchronized OTP
    public static String generateOTP() {
       
        long currentTimeMinutes = System.currentTimeMillis() / (1000 * 60);
        Random random = new Random(currentTimeMinutes);
        
        //6 digit number
        int otp = random.nextInt(900000) + 100000;
        
        System.out.println("Generated OTP: " + otp + " (Seed: " + currentTimeMinutes + ")");
        return String.valueOf(otp);
    }
    
   //Method for verifying OTP
    public static boolean verifyOTP(String enteredOTP) {
        String expectedOTP = generateOTP();
        boolean isValid = expectedOTP.equals(enteredOTP);
        
        System.out.println("Expected OTP: " + expectedOTP);
        System.out.println("Entered OTP: " + enteredOTP);
        System.out.println("OTP Valid: " + isValid);
        
        return isValid;
    }
    
    // For testing - generate OTP with custom seed
    public static String generateOTPWithSeed(long seed) {
        Random random = new Random(seed);
        int otp = random.nextInt(900000) + 100000;
        System.out.println("Generated OTP with seed " + seed + ": " + otp);
        return String.valueOf(otp);
    }
}