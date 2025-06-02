package service;

import java.sql.SQLException;

import entity.AuthenticationResult;
import entity.Customer;
import entity.Employee;
import entity.Manager;
import storage.DataStorage;
import util.PasswordUtil;

public class AuthenticationService {
    private final DataStorage dataStorage;
    
    public AuthenticationService(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }
    
    public AuthenticationResult authenticate(String email, String password) throws SQLException {

        Customer customer = dataStorage.getCustomerByEmail(email);
        if (customer != null) {
            String salt = dataStorage.getSaltForCustomer(customer.getId());
            if (salt == null) {
                System.out.println("Salt not found for customer ID: " + customer.getId());
            }
            if (PasswordUtil.verifyPassword(password, salt, customer.getPassword())) {
                return new AuthenticationResult(true, "CUSTOMER", customer.getId(), customer.getName());
            }
        }

        
     
        Employee employee = dataStorage.getEmployeeByEmail(email);
        if (employee != null) {
            String salt = dataStorage.getSaltForEmployee(employee.getId());
            if (PasswordUtil.verifyPassword(password, salt, employee.getPassword())) {
                String role = (employee instanceof Manager) ? "MANAGER" : "EMPLOYEE";
                return new AuthenticationResult(true, role, employee.getId(), employee.getName());
            }
        }

        
        return new AuthenticationResult(false, null, 0, null);
    }
    
    
}