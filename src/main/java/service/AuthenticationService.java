package service;

import java.sql.SQLException;

import entity.AuthenticationResult;
import entity.Customer;
import entity.Employee;
import entity.Manager;
import storage.DataStorage;
import util.PasswordUtil;

public class AuthenticationService {
	
	//Data storage is required to get the details based on users choice
    private final DataStorage dataStorage;
    
    public AuthenticationService(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }
    
    //Gets the user and authenticate them using the stored password and salt
    public AuthenticationResult authenticate(String email, String password) throws SQLException {
    	
    	//Authentication if the user is a Customer
        Customer customer = dataStorage.getCustomerByEmail(email);
        if (customer != null) {
            String salt = dataStorage.getSaltForCustomer(customer.getId());
            if (salt == null) {
                System.out.println("Salt not found for customer ID: " + customer.getId());
            }
            //Utilizes Password utility function
            if (PasswordUtil.verifyPassword(password, salt, customer.getPassword())) {
                return new AuthenticationResult(true, "CUSTOMER", customer.getId(), customer.getName());
            }
        }

        
    	//Authentication if the user is an Employee

        Employee employee = dataStorage.getEmployeeByEmail(email);
        if (employee != null) {
            String salt = dataStorage.getSaltForEmployee(employee.getId());
            if (PasswordUtil.verifyPassword(password, salt, employee.getPassword())) {
                String role = (employee instanceof Manager) ? "MANAGER" : "EMPLOYEE";
                return new AuthenticationResult(true, role, employee.getId(), employee.getName());
            }
        }

        //If the user is not there in the data base
        return new AuthenticationResult(false, null, 0, null);
    }
    
    
}