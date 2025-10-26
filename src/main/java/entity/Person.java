package entity;

/**
 * Represents a person entity in the banking system.
 * <p><strong>Implemented by:</strong></p>
 * <ul>
 *   <li>{@link Customer} - Bank customers who hold accounts</li>
 *   <li>{@link Employee} - Bank staff members</li>
 *   <li>{@link Manager} - Bank managers with administrative privileges</li>
 * </ul>
 * 
 * @author TAMIL MUGHILAN
 * @see Customer
 * @see Employee
 * @see Manager
 */
public interface Person {

    /**
     * Gets the person's unique ID.
     * 
     * @return the person's ID
     */
    int getId();

    /**
     * Gets the person's name.
     * 
     * @return the person's name
     */
    String getName();

    /**
     * Gets the person's email address.
     * 
     * @return the email address
     */
    String getEmail();

    /**
     * Validates the person's email address.
     * 
     * @return {@code true} if the email is valid; {@code false} otherwise
     */
    default boolean isValidEmail() {
        String email = getEmail();
        return email != null && email.contains("@") && email.contains(".");
    }

    /**
     * Validates the person's name.
     * 
     * @return {@code true} if the name is valid; {@code false} otherwise
     */
    default boolean isValidName() {
        String name = getName();
        return name != null && !name.trim().isEmpty();
    }
}
