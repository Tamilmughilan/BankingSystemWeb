package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility class for password hashing and verification
 * Uses SHA-256 with salt for secure password storage
 * 
 * @author TAMIL MUGHILAN
 */
public class PasswordUtil {
    
	/**
     * Hashes a password using SHA-256 with the provided salt.
     *
     * @param password the plain text password to hash
     * @param salt the salt to use for hashing
     * @return the hashed password as hexadecimal string
     * @throws RuntimeException if SHA-256 algorithm is not available
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Generates a secure random salt.
     * Uses SecureRandom to generate 16 random bytes.
     *
     * @return the generated salt as hexadecimal string
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        
        StringBuilder sb = new StringBuilder(); 
        for (byte b : salt) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * Verifies if a password matches the stored hash.
     * Hashes the provided password with salt and compares.
     *
     * @param password the plain text password to verify
     * @param salt the salt used for the stored hash
     * @param hashedPassword the stored hashed password
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String salt, String hashedPassword) {
        if (salt == null || password == null || hashedPassword == null) {
            return false;
        }
        String computedHash = hashPassword(password, salt);
        return computedHash.equals(hashedPassword);
    }

}