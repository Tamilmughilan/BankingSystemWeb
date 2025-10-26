package entity;

/**
 * Represents the result of an authentication attempt.
 * Stores the outcome along with user details such as role, ID, and name.
 * 
 * 
 * @author TAMIL MUGHILAN
 */
public class AuthenticationResult {

    // Attributes
    private final boolean success;
    private final String role;
    private final int userId;
    private final String userName;

    /**
     * Constructs an {@code AuthenticationResult} object.
     * 
     * @param success   {@code true} if authentication was successful
     * @param role      role of the authenticated user (e.g., "CUSTOMER", "EMPLOYEE", "MANAGER")
     * @param userId    ID of the authenticated user
     * @param userName  name of the authenticated user
     */
    public AuthenticationResult(boolean success, String role, int userId, String userName) {
        this.success = success;
        this.role = role;
        this.userId = userId;
        this.userName = userName;
    }

    /**
     * Returns whether the authentication was successful.
     * 
     * @return {@code true} if successful; {@code false} otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the role of the authenticated user.
     * 
     * @return the user's role
     */
    public String getRole() {
        return role;
    }

    /**
     * Returns the ID of the authenticated user.
     * 
     * @return the user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Returns the name of the authenticated user.
     * 
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }
}
