package storage;

import entity.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface DataStorage {
    
    // Customer operations
    int saveCustomer(Customer customer);
    int saveCustomerWithPassword(Customer customer, String salt);
    void updateCustomer(Customer customer) throws SQLException;
    Customer getCustomer(int customerId);
    Customer getCustomerByEmail(String email);
    boolean deleteCustomer(int customerId);
    List<Customer> getCustomersByBranch(int branchId);
    String getSaltForCustomer(int customerId);
    
    // Employee operations
    int saveEmployee(Employee employee);
    int saveEmployeeWithPassword(Employee employee, String salt);
    Employee getEmployee(int employeeId);
    Employee getEmployeeByEmail(String email);
    Manager getManager(int employeeId);
    String getSaltForEmployee(int employeeId);
    
    // Account operations
    int saveAccount(SavingsAccount account);
    SavingsAccount getAccount(int accountNo);
    void updateAccount(SavingsAccount account);
    boolean deleteAccount(int accountNo);
    List<SavingsAccount> getAccountsByBranch(int branchId);
    List<SavingsAccount> getAccountsByCustomer(int customerId);
    
    // Transaction operations
    boolean withdrawFromAccount(int accountNo, BigDecimal amount);
    boolean depositToAccount(int accountNo, BigDecimal amount);
    
    // Branch operations
    void saveBranch(Branch branch);
    Branch getBranch(int branchId);
    
    // Joint account operations (default implementations for non-database storage)
    default int addCustomerToAccount(int customerId, int accountNo, String role) {
        throw new UnsupportedOperationException("Joint accounts are not supported by this storage implementation");
    }
    
    default boolean removeCustomerFromAccount(int customerId, int accountNo) {
        throw new UnsupportedOperationException("Joint accounts are not supported by this storage implementation");
    }
    
    default List<Customer> getCustomersByAccount(int accountNo) {
        throw new UnsupportedOperationException("Joint accounts are not supported by this storage implementation");
    }
}