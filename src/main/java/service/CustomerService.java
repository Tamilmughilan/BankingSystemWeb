package service;

import java.sql.SQLException;
import java.util.List;

import entity.Customer;
import storage.DataStorage;

/**
 * Service class for managing customer operations.
 * Handles customer creation, updates, retrieval, and deletion.
 *
 * @author TAMIL MUGHILAN
 */
public class CustomerService {
    private DataStorage dataStorage;
    
    /**
     * Creates a new CustomerService with the specified data storage.
     *
     * @param dataStorage the data storage implementation to use
     */
    public CustomerService(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }
    
    /**
     * Creates a new customer.
     *
     * @param name the customer's name
     * @param phone the customer's phone number
     * @param email the customer's email address
     * @param branchId the branch ID
     * @return the new customer ID
     */
    public int createCustomer(String name, String phone, String email, int branchId) {
        Customer customer = new Customer.Builder(name, email, branchId)
                                      .phone(phone)
                                      .build();
        return dataStorage.saveCustomer(customer);
    }

    /**
     * Creates a new customer with password for login.
     *
     * @param name the customer's name
     * @param phone the customer's phone number
     * @param email the customer's email address
     * @param branchId the branch ID
     * @param password the hashed password
     * @param salt the password salt for security
     * @return the new customer ID
     */
    public int createCustomerWithPassword(String name, String phone, String email, 
                                        int branchId, String password, String salt) {
        Customer customer = new Customer.Builder(name, email, branchId)
                                      .phone(phone)
                                      .password(password)
                                      .salt(salt)
                                      .build();
        return dataStorage.saveCustomerWithPassword(customer, salt);
    }

    /**
     * Updates an existing customer's information.
     *
     * @param customerId the customer ID to update
     * @param name the new name
     * @param phone the new phone number
     * @param email the new email address
     * @param branchId the new branch ID
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if customer data is invalid
     */
    public void updateCustomer(int customerId, String name, String phone, 
            String email, int branchId) throws SQLException {
    	Customer customer = new Customer.Builder(name, email, branchId)
                .id(customerId)
                .phone(phone)
                .build();			if (!customer.isValidCustomer()) {
			throw new IllegalArgumentException("Invalid customer data");
			}
			dataStorage.updateCustomer(customer);
			}


    /**
     * Retrieves a customer by ID.
     *
     * @param customerId the customer ID
     * @return the customer or null if not found
     */
    public Customer getCustomer(int customerId) {
        return dataStorage.getCustomer(customerId);
    }
    
    /**
     * Retrieves a customer by ID.
     *
     * @param customerId the customer ID
     * @return the customer or null if not found
     */
    public boolean deleteCustomer(int customerId) {
        return dataStorage.deleteCustomer(customerId);
    }
    
    /**
     * Retrieves a customer by email address.
     *
     * @param email the customer's email address
     * @return the customer or null if not found
     */
    public Customer getCustomerByEmail(String email) {
        return dataStorage.getCustomerByEmail(email);
    }
  
    /**
     * Gets all customers for a specific branch.
     *
     * @param branchId the branch ID
     * @return list of customers in the branch
     */
    public List<Customer> getCustomersByBranch(int branchId) {
        return dataStorage.getCustomersByBranch(branchId);
    }
    
    /**
     * Gets the password salt for a customer.
     *
     * @param customerId the customer ID
     * @return the password salt or null if not found
     */
    public String getSaltForCustomer(int customerId) { 
        return dataStorage.getSaltForCustomer(customerId);
    }
}