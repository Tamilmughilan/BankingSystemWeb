package service;

import java.sql.SQLException;

import entity.Customer;
import storage.DataStorage;

public class CustomerService {
    private DataStorage dataStorage;
    
    public CustomerService(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }
    
    public int createCustomer(String name, String phone, String email, int branchId) {
        // Business logic validation
        Customer customer = new Customer(0, name, phone, email, branchId);
        
        if (!customer.isValidCustomer()) {
            throw new IllegalArgumentException("Invalid customer data provided");
        }
        
        // Delegate to storage layer
        return dataStorage.saveCustomer(customer);
    }
    
    public int createCustomerWithPassword(String name, String phone, String email, int branchId, 
                                        String hashedPassword, String salt) {
       
        Customer customer = new Customer(0, name, phone, email, branchId);
        customer.setPassword(hashedPassword);        
        if (!customer.isValidCustomer()) {
            throw new IllegalArgumentException("Invalid customer data provided");
        }
        
        // Delegate to storage layer
        return dataStorage.saveCustomerWithPassword(customer, salt);
    }
    public void updateCustomer(int customerId, String name, String phone, 
            String email, int branchId) throws SQLException {
			Customer customer = new Customer(customerId, name, phone, email, branchId);
			if (!customer.isValidCustomer()) {
			throw new IllegalArgumentException("Invalid customer data");
			}
			dataStorage.updateCustomer(customer);
			}


    
    public Customer getCustomer(int customerId) {
        return dataStorage.getCustomer(customerId);
    }
    
    public boolean deleteCustomer(int customerId) {
        return dataStorage.deleteCustomer(customerId);
    }
}