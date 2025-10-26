package entity;

/**
 * Abstract base class for customers in the banking system.
 * Implements common attributes and validation logic.
 * 
 * @author TAMIL MUGHILAN
 */
public abstract class AbstractCustomer implements Person {
    protected int id;
    protected String name;
    protected String phone;
    protected String email;
    protected String password;

    /**
     * Constructs an AbstractCustomer with the given details.
     * 
     * @param id      customer ID
     * @param name    customer name
     * @param phone   customer phone number
     * @param email   customer email address
     */
    public AbstractCustomer(int id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the branch ID.
     * 
     * @return branch ID
     */
    public abstract int getBranchId();

    /**
     * Validates phone number.
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValidPhone() {
        return phone != null && phone.length() >= 10;
    }

    /**
     * Validates customer data.
     * 
     * @return true if all fields are valid
     */
    public boolean isValidCustomer() {
        return isValidName() && isValidEmail() && isValidPhone();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
}
