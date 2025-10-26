package storage;

import entity.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public abstract class AbstractDataStorage implements DataStorage {

    // Common email validation logic
    protected boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // Customer operations
    public abstract int saveCustomer(Customer customer);
    /**
     * Saves a new customer with password for login capability.
     *
     * @param customer the customer to save
     * @param salt the password salt for security
     * @return the generated customer ID
     */
    public abstract int saveCustomerWithPassword(Customer customer, String salt);
    
    /**
     * Retrieves a customer by ID.
     *
     * @param customerId the customer ID
     * @return the customer or null if not found
     */
    public abstract Customer getCustomer(int customerId);
    
    /**
     * Retrieves a customer by email address.
     *
     * @param email the customer's email
     * @return the customer or null if not found
     */
    public abstract Customer getCustomerByEmail(String email);
    
    /**
     * Deletes a customer by ID.
     *
     * @param customerId the customer ID to delete
     * @return true if deletion was successful, false otherwise
     */
    public abstract boolean deleteCustomer(int customerId);
    
    /**
     * Gets all customers for a specific branch.
     *
     * @param branchId the branch ID
     * @return list of customers in the branch
     */
    public abstract List<Customer> getCustomersByBranch(int branchId);
    
    /**
     * Gets the password salt for a customer.
     *
     * @param customerId the customer ID
     * @return the password salt or null if not found
     */
    public abstract String getSaltForCustomer(int customerId);

    // Employee operations
    /**
     * Saves a new employee to storage.
     *
     * @param employee the employee to save
     * @return the generated employee ID
     */
    public abstract int saveEmployee(Employee employee);
    
    /**
     * Saves a new employee with password for login capability.
     *
     * @param employee the employee to save
     * @param salt the password salt for security
     * @return the generated employee ID
     */
    public abstract int saveEmployeeWithPassword(Employee employee, String salt);
    
    /**
     * Retrieves an employee by ID.
     *
     * @param employeeId the employee ID
     * @return the employee or null if not found
     */
    public abstract Employee getEmployee(int employeeId);
    
    /**
     * Retrieves an employee by email address.
     *
     * @param email the employee's email
     * @return the employee or null if not found
     */
    public abstract Employee getEmployeeByEmail(String email);
    public abstract Manager getManager(int employeeId);
    @Override
    public abstract String getSaltForEmployee(int employeeId) ; 
    
    
    // Account operations
    /**
     * Saves a new account to storage.
     *
     * @param account the account to save
     * @return the generated account number
     */
    public abstract int saveAccount(SavingsAccount account);
    
    /**
     * Retrieves an account by account number.
     *
     * @param accountNo the account number
     * @return the account or null if not found
     */
    public abstract SavingsAccount getAccount(int accountNo);
    
    /**
     * Updates an existing account.
     *
     * @param account the account to update
     */
    public abstract void updateAccount(SavingsAccount account);
    
    /**
     * Deletes an account by account number.
     *
     * @param accountNo the account number to delete
     * @return true if deletion was successful, false otherwise
     */
    public abstract boolean deleteAccount(int accountNo);
    
    /**
     * Gets all accounts for a specific branch.
     *
     * @param branchId the branch ID
     * @return list of accounts in the branch
     */
    public abstract List<SavingsAccount> getAccountsByBranch(int branchId);
    public abstract List<SavingsAccount> getAccountsByCustomer(int customerId);
    
    // Branch operations
    /**
     * Saves a branch to storage.
     *
     * @param branch the branch to save
     */
    public abstract void saveBranch(Branch branch);
    
    /**
     * Retrieves a branch by ID.
     *
     * @param branchId the branch ID
     * @return the branch or null if not found
     */
    public abstract Branch getBranch(int branchId);

    // Transaction operations
    /**
     * Withdraws money from an account.
     *
     * @param accountNo the account number
     * @param amount the amount to withdraw
     * @return true if withdrawal was successful, false otherwise
     */
    public abstract boolean withdrawFromAccount(int accountNo, BigDecimal amount);
    
    /**
     * Deposits money to an account.
     *
     * @param accountNo the account number
     * @param amount the amount to deposit
     * @return true if deposit was successful, false otherwise
     */
    public abstract boolean depositToAccount(int accountNo, BigDecimal amount);

    /**
     * Updates an existing customer's information.
     *
     * @param customer the customer to update
     * @throws SQLException if database operation fails
     */
    @Override
    public abstract void updateCustomer(Customer customer) throws SQLException;
}