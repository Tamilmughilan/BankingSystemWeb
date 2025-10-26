package storage;

import entity.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface defining data storage operations for the banking system.
 * Provides contract for customer, employee, account, and transaction operations.
 * Supports multiple storage implementations (database, collection, mongoDB).
 *
 * @author TAMIL MUGHILAN
 */
public interface DataStorage {
    
	/**
     * Saves a new customer to storage.
     *
     * @param customer the customer to save
     * @return the generated customer ID
     */
    int saveCustomer(Customer customer);
    
    /**
     * Saves a new customer with password for login capability.
     *
     * @param customer the customer to save
     * @param salt the password salt for security
     * @return the generated customer ID
     */
    int saveCustomerWithPassword(Customer customer, String salt);
    
    /**
     * Updates an existing customer's information.
     *
     * @param customer the customer to update
     * @throws SQLException if database operation fails
     */
    void updateCustomer(Customer customer) throws SQLException;
    
    /**
     * Retrieves a customer by ID.
     *
     * @param customerId the customer ID
     * @return the customer or null if not found
     */
    Customer getCustomer(int customerId);
    
    /**
     * Retrieves a customer by email address.
     *
     * @param email the customer's email
     * @return the customer or null if not found
     */
    Customer getCustomerByEmail(String email);
    
    /**
     * Deletes a customer by ID.
     *
     * @param customerId the customer ID to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteCustomer(int customerId);
    
    /**
     * Gets all customers for a specific branch.
     *
     * @param branchId the branch ID
     * @return list of customers in the branch
     */
    List<Customer> getCustomersByBranch(int branchId);
    
    /**
     * Gets the password salt for a customer.
     *
     * @param customerId the customer ID
     * @return the password salt or null if not found
     */
    String getSaltForCustomer(int customerId);
    
    // Employee operations
    /**
     * Saves a new employee to storage.
     *
     * @param employee the employee to save
     * @return the generated employee ID
     */
    int saveEmployee(Employee employee);
    
    /**
     * Saves a new employee with password for login capability.
     *
     * @param employee the employee to save
     * @param salt the password salt for security
     * @return the generated employee ID
     */
    int saveEmployeeWithPassword(Employee employee, String salt);
    
    /**
     * Retrieves an employee by ID.
     *
     * @param employeeId the employee ID
     * @return the employee or null if not found
     */
    Employee getEmployee(int employeeId);
    
    /**
     * Retrieves an employee by email address.
     *
     * @param email the employee's email
     * @return the employee or null if not found
     */
    Employee getEmployeeByEmail(String email);
    Manager getManager(int employeeId);
    String getSaltForEmployee(int employeeId);
    
    // Account operations
    /**
     * Saves a new account to storage.
     *
     * @param account the account to save
     * @return the generated account number
     */
    int saveAccount(SavingsAccount account);
    
    /**
     * Retrieves an account by account number.
     *
     * @param accountNo the account number
     * @return the account or null if not found
     */
    SavingsAccount getAccount(int accountNo);
    
    /**
     * Updates an existing account.
     *
     * @param account the account to update
     */
    void updateAccount(SavingsAccount account);
    
    /**
     * Deletes an account by account number.
     *
     * @param accountNo the account number to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteAccount(int accountNo);
    
    /**
     * Gets all accounts for a specific branch.
     *
     * @param branchId the branch ID
     * @return list of accounts in the branch
     */
    List<SavingsAccount> getAccountsByBranch(int branchId);
    List<SavingsAccount> getAccountsByCustomer(int customerId);
    
    // Transaction operations
    /**
     * Withdraws money from an account.
     *
     * @param accountNo the account number
     * @param amount the amount to withdraw
     * @return true if withdrawal was successful, false otherwise
     */
    boolean withdrawFromAccount(int accountNo, BigDecimal amount);
    
    /**
     * Deposits money to an account.
     *
     * @param accountNo the account number
     * @param amount the amount to deposit
     * @return true if deposit was successful, false otherwise
     */
    boolean depositToAccount(int accountNo, BigDecimal amount);
    
    // Branch operations
    /**
     * Saves a branch to storage.
     *
     * @param branch the branch to save
     */
    void saveBranch(Branch branch);
    
    /**
     * Retrieves a branch by ID.
     *
     * @param branchId the branch ID
     * @return the branch or null if not found
     */
    Branch getBranch(int branchId);
    
    // Joint account operations
    /**
     * Adds a customer to an existing account as joint holder.
     * Default implementation throws UnsupportedOperationException.
     *
     * @param customerId the customer ID to add
     * @param accountNo the account number
     * @param role the customer's role (PRIMARY, JOINT)
     * @return the result status
     * @throws UnsupportedOperationException if not supported by implementation
     */
    default int addCustomerToAccount(int customerId, int accountNo, String role) {
        throw new UnsupportedOperationException("Joint accounts are not supported by this storage implementation");
    }
    
    /**
     * Removes a customer from a joint account.
     * Default implementation throws UnsupportedOperationException.
     *
     * @param customerId the customer ID to remove
     * @param accountNo the account number
     * @return true if removal was successful, false otherwise
     * @throws UnsupportedOperationException if not supported by implementation
     */

    default boolean removeCustomerFromAccount(int customerId, int accountNo) {
        throw new UnsupportedOperationException("Joint accounts are not supported by this storage implementation");
    }
    
    /**
     * Gets all customers who are holders of an account.
     * Default implementation throws UnsupportedOperationException.
     *
     * @param accountNo the account number
     * @return list of account holders
     * @throws UnsupportedOperationException if not supported by implementation
     */
    default List<Customer> getCustomersByAccount(int accountNo) {
        throw new UnsupportedOperationException("Joint accounts are not supported by this storage implementation");
    }
    
    // Transaction logging operations
    /**
     * Logs a transaction for audit purposes.
     * Default implementation returns true (no-op).
     *
     * @param transactionLog the transaction log to save
     * @return true if logging was successful, false otherwise
     */
    default boolean logTransaction(TransactionLog transactionLog) {
        return true;
    }
    
    
    default List<TransactionLog> getTransactionHistory(int accountNo) {
        throw new UnsupportedOperationException("Transaction history is not supported by this storage implementation");
    }
    
    /**
     * Gets transaction history for an account with specified limit.
     * Default implementation throws UnsupportedOperationException.
     *
     * @param accountNo the account number
     * @param limit maximum number of records to retrieve
     * @return list of transaction logs
     * @throws UnsupportedOperationException if not supported by implementation
     */
    default List<TransactionLog> getTransactionHistory(int accountNo, int limit) {
        throw new UnsupportedOperationException("Transaction history is not supported by this storage implementation");
    }
    
    /**
     * Gets transaction history for an account within date range.
     * Default implementation throws UnsupportedOperationException.
     *
     * @param accountNo the account number
     * @param startDate the start date
     * @param endDate the end date
     * @return list of transaction logs
     * @throws UnsupportedOperationException if not supported by implementation
     */
    default List<TransactionLog> getTransactionHistoryByDateRange(int accountNo, 
                                                                  java.time.LocalDateTime startDate, 
                                                                  java.time.LocalDateTime endDate) {
        throw new UnsupportedOperationException("Transaction history by date range is not supported by this storage implementation");
    }
}