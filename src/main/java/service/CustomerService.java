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
        Customer customer = new Customer.Builder(name, email, branchId)
                                      .phone(phone)
                                      .build();
        return dataStorage.saveCustomer(customer);
    }

    public int createCustomerWithPassword(String name, String phone, String email, 
                                        int branchId, String password, String salt) {
        Customer customer = new Customer.Builder(name, email, branchId)
                                      .phone(phone)
                                      .password(password)
                                      .salt(salt)
                                      .build();
        return dataStorage.saveCustomerWithPassword(customer, salt);
    }

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


    
    public Customer getCustomer(int customerId) {
        return dataStorage.getCustomer(customerId);
    }
    
    public boolean deleteCustomer(int customerId) {
        return dataStorage.deleteCustomer(customerId);
    }
}