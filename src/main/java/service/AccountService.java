package service;

import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import entity.Customer;
import entity.SavingsAccount;
import storage.DataStorage;
import storage.DatabaseStorage;

public class AccountService {
    private final DataStorage dataStorage;

    public AccountService(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    public int createSavingsAccount(int customerId, BigDecimal initialBalance, int branchID) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }

        BigDecimal minBalance = new BigDecimal("100.00");
        if (initialBalance.compareTo(minBalance) < 0) {
            throw new IllegalArgumentException("Initial balance must be at least 100 for savings account");
        }

        SavingsAccount account = new SavingsAccount.Builder(customerId, branchID)
                .balance(initialBalance)
                .build();
        if (!account.isValidAccount()) {
            throw new IllegalArgumentException("Invalid account data");
        }

        return dataStorage.saveAccount(account);
    }


    public SavingsAccount getAccount(int accountNo) {
        return dataStorage.getAccount(accountNo);
    }

    public void updateAccount(SavingsAccount account) {
        dataStorage.updateAccount(account);
    }

    public boolean deleteAccount(int accountNo) {
        return dataStorage.deleteAccount(accountNo);
    }
    
    public List<SavingsAccount> getAccountsByBranch(int branchId) {
        return dataStorage.getAccountsByBranch(branchId);
    }
    
    // New method to get accounts by customer
    public List<SavingsAccount> getAccountsByCustomer(int customerId) {
        return dataStorage.getAccountsByCustomer(customerId);
    }

    public boolean performWithdrawal(int accountNo, BigDecimal amount) {
        //For database - transaction
        if (dataStorage instanceof DatabaseStorage) {
            return ((DatabaseStorage) dataStorage).withdrawFromAccount(accountNo, amount);
        }

        //for collection storage
        SavingsAccount account = dataStorage.getAccount(accountNo);
        if (account == null) {
            return false;
        }

        if (account.withdraw(amount)) {
            dataStorage.updateAccount(account);
            return true;
        }

        return false;
    }

    public boolean performDeposit(int accountNo, BigDecimal amount) {
        if (dataStorage instanceof DatabaseStorage) {
            return ((DatabaseStorage) dataStorage).depositToAccount(accountNo, amount);
        }

        SavingsAccount account = dataStorage.getAccount(accountNo);
        if (account != null) {
            account.deposit(amount);
            dataStorage.updateAccount(account);
            return true;
        }
        return false;
    }
 
    public int createJointSavingsAccount(List<Integer> customerIds, BigDecimal initialBalance, int branchId) {
        if (customerIds == null || customerIds.isEmpty()) {
            throw new IllegalArgumentException("At least one customer ID is required");
        }
        
        BigDecimal minBalance = new BigDecimal("100.00");
        if (initialBalance.compareTo(minBalance)< 0) {
            throw new IllegalArgumentException("Initial balance must be at least 100 for savings account");
        }
        
        // Verify all customers exist before creating account
        for (Integer customerId : customerIds) {
            Customer customer = dataStorage.getCustomer(customerId);
            if (customer == null) {
                throw new IllegalArgumentException("Customer with ID " + customerId + " does not exist");
            }
        }
        
        // Create account with the first customer as primary 
        int primaryCustomerId = customerIds.get(0);
        SavingsAccount account = new SavingsAccount.Builder(primaryCustomerId, branchId)
                .balance(initialBalance)
                .build();
        
        if (!account.isValidAccount()) {
            throw new IllegalArgumentException("Invalid account data");
        }
        
        int accountNo = dataStorage.saveAccount(account);
        
        if (accountNo <= 0) {
            throw new RuntimeException("Failed to create account");
        }
        
        
        if (dataStorage instanceof DatabaseStorage) {
            DatabaseStorage dbStorage = (DatabaseStorage) dataStorage;
            for (int i = 0; i < customerIds.size(); i++) {
                String role = (i == 0) ? "PRIMARY" : "JOINT";
                int result = dbStorage.addCustomerToAccount(customerIds.get(i), accountNo, role);
                if (result <= 0) {
                    
                    System.err.println("Failed to add customer " + customerIds.get(i) + " to account " + accountNo);
                }
            }
        }
        
        return accountNo;
    }
    
    
    public int createJointSavingsAccount(String customerIdsStr, BigDecimal initialBalance, int branchId) {
        if (customerIdsStr == null || customerIdsStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer IDs cannot be empty");
        }
        
        List<Integer> customerIds = new ArrayList<>();
        String[] idStrings = customerIdsStr.split(",");
        
        for (String idStr : idStrings) {
            try {
                int customerId = Integer.parseInt(idStr.trim());
                if (customerId > 0) {
                    customerIds.add(customerId);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid customer ID format: " + idStr);
            }
        }
        
        return createJointSavingsAccount(customerIds, initialBalance, branchId);
    }

    public boolean addCustomerToExistingAccount(int customerId, int accountNo) {
        // Only works with DatabaseStorage
        if (!(dataStorage instanceof DatabaseStorage)) {
            throw new UnsupportedOperationException("Joint accounts are only supported with database storage");
        }
        
        // Verify customer exists
        Customer customer = dataStorage.getCustomer(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer with ID " + customerId + " does not exist");
        }
        
        // Verify account exists
        SavingsAccount account = dataStorage.getAccount(accountNo);
        if (account == null) {
            throw new IllegalArgumentException("Account with number " + accountNo + " does not exist");
        }
        
        DatabaseStorage dbStorage = (DatabaseStorage) dataStorage;
        return dbStorage.addCustomerToAccount(customerId, accountNo, "JOINT") > 0;
    }

    public boolean removeCustomerFromAccount(int customerId, int accountNo) {
        // Only works with DatabaseStorage
        if (!(dataStorage instanceof DatabaseStorage)) {
            throw new UnsupportedOperationException("Joint accounts are only supported with database storage");
        }
        
        DatabaseStorage dbStorage = (DatabaseStorage) dataStorage;
        
        // Ensure at least one customer remains
        List<Customer> customers = dbStorage.getCustomersByAccount(accountNo);
        if (customers.size() <= 1) {
            throw new IllegalStateException("Cannot remove the last customer from an account");
        }
        
        return dbStorage.removeCustomerFromAccount(customerId, accountNo);
    }

    public List<Customer> getAccountHolders(int accountNo) {
        // Only works with DatabaseStorage
        if (!(dataStorage instanceof DatabaseStorage)) {
            throw new UnsupportedOperationException("Joint accounts are only supported with database storage");
        }
        
        DatabaseStorage dbStorage = (DatabaseStorage) dataStorage;
        return dbStorage.getCustomersByAccount(accountNo);
    }
}