package service;

import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import entity.Customer;
import entity.SavingsAccount;
import entity.TransactionLog;
import storage.DataStorage;
import storage.DatabaseStorage;

/**
 * Service class for managing bank account operations.
 * Handles account creation, transactions, and joint account operations.
 *
 * @author TAMIL MUGHILAN
 */
public class AccountService {
    private final DataStorage dataStorage;
    
    /**
     * Creates a new AccountService with the specified data storage based on user's choice.
     *
     * @param dataStorage the data storage implementation to use
     */
    public AccountService(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Creates a new savings account for a customer.
     *
     * @param customerId the customer ID
     * @param initialBalance the initial balance (minimum Rs.100)
     * @param branchID the branch ID
     * @return the new account number
     * @throws IllegalArgumentException if parameters are invalid
     */
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


    /**
     * Retrieves an account by account number.
     *
     * @param accountNo the account number
     * @return the savings account or null if not found
     */
    public SavingsAccount getAccount(int accountNo) {
        return dataStorage.getAccount(accountNo);
    }
    
    /**
     * Updates an existing account.
     *
     * @param account the account to update
     */
    public void updateAccount(SavingsAccount account) {
        dataStorage.updateAccount(account);
    }

    /**
    * Deletes an account by account number.
    *
    * @param accountNo the account number to delete
    * @return true if deletion was successful, false otherwise
    */
    public boolean deleteAccount(int accountNo) {
        return dataStorage.deleteAccount(accountNo);
    }
    
    /**
     * Gets all accounts for a specific branch.
     *
     * @param branchId the branch ID
     * @return list of accounts in the branch
     */
    public List<SavingsAccount> getAccountsByBranch(int branchId) {
        return dataStorage.getAccountsByBranch(branchId);
    }
    
    /**
     * Gets all accounts for a specific customer.
     *
     * @param customerId the customer ID
     * @return list of customer's accounts
     */
    public List<SavingsAccount> getAccountsByCustomer(int customerId) {
        return dataStorage.getAccountsByCustomer(customerId);
    }
    
    public boolean performWithdrawal(int accountNo, BigDecimal amount) {
        return performWithdrawal(accountNo, amount, null, null, null);
    }

    public boolean performDeposit(int accountNo, BigDecimal amount) {
        return performDeposit(accountNo, amount, null, null, null);
    }

    
    /**
     * Performs a withdrawal with transaction logging.
     *
     * @param accountNo the account number
     * @param amount the amount to withdraw
     * @param userId the user performing the transaction
     * @param userType the type of user (CUSTOMER, EMPLOYEE, MANAGER)
     * @param description description of the transaction
     * @return true if withdrawal was successful, false otherwise
     */
    public boolean performWithdrawal(int accountNo, BigDecimal amount, Integer userId,TransactionLog.UserType userType, String description) {
			if (dataStorage instanceof DatabaseStorage) {
				DatabaseStorage dbStorage = (DatabaseStorage) dataStorage;
				return dbStorage.withdrawFromAccount(accountNo, amount, userId, userType, description);
			}
			
			// For collection storage - original logic
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

    
    /**
     * Performs a deposit with transaction logging.
     *
     * @param accountNo the account number
     * @param amount the amount to deposit
     * @param userId the user performing the transaction
     * @param userType the type of user (CUSTOMER, EMPLOYEE, MANAGER)
     * @param description description of the transaction
     * @return true if deposit was successful, false otherwise
     */
    public boolean performDeposit(int accountNo, BigDecimal amount, Integer userId, TransactionLog.UserType userType, String description) {
		if (dataStorage instanceof DatabaseStorage) {
		DatabaseStorage dbStorage = (DatabaseStorage) dataStorage;
		return dbStorage.depositToAccount(accountNo, amount, userId, userType, description);
		}
		
		// For collection storage - original logic
		SavingsAccount account = dataStorage.getAccount(accountNo);
		if (account != null) {
			account.deposit(amount);
			dataStorage.updateAccount(account);
			return true;
		}
		return false;
		}
 
    
    /**
     * Creates a joint savings account for multiple customers.
     *
     * @param customerIds list of customer IDs for the joint account
     * @param initialBalance the initial balance (minimum Rs.100)
     * @param branchId the branch ID
     * @return the new account number
     * @throws IllegalArgumentException if parameters are invalid
     */
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
    
    
    /**
     * Creates a joint savings account from comma separated customer IDs.
     *
     * @param customerIdsStr comma-separated customer IDs (e.g., "1,2,3")
     * @param initialBalance the initial balance (minimum Rs.100)
     * @param branchId the branch ID
     * @return the new account number
     * @throws IllegalArgumentException if parameters are invalid
     */
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

    /**
     * Adds a customer to an existing account as joint holder.
     *
     * @param customerId the customer ID to add
     * @param accountNo the account number
     * @return true if customer was added successfully, false otherwise
     * @throws IllegalArgumentException if customer or account doesn't exist
     */
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

    
    /**
     * Removes a customer from a joint account.
     *
     * @param customerId the customer ID to remove
     * @param accountNo the account number
     * @return true if customer was removed successfully, false otherwise
     * @throws IllegalStateException if trying to remove the last customer
     */
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

    /**
     * Gets all customers who are holders of an account.
     *
     * @param accountNo the account number
     * @return list of customers who hold the account
     */
    public List<Customer> getAccountHolders(int accountNo) {
        // Only works with DatabaseStorage
        if (!(dataStorage instanceof DatabaseStorage)) {
            throw new UnsupportedOperationException("Joint accounts are only supported with database storage");
        }
        
        DatabaseStorage dbStorage = (DatabaseStorage) dataStorage;
        return dbStorage.getCustomersByAccount(accountNo);
    }
    
    /**
     * Gets transaction history for an account (default 50 records).
     *
     * @param accountNo the account number
     * @return list of transaction logs
     */
    public List<TransactionLog> getTransactionHistory(int accountNo) {
        return dataStorage.getTransactionHistory(accountNo);
    }

    /**
     * Gets transaction history for an account with specified limit.
     *
     * @param accountNo the account number
     * @param limit maximum number of records to retrieve
     * @return list of transaction logs
     */
    public List<TransactionLog> getTransactionHistory(int accountNo, int limit) {
        return dataStorage.getTransactionHistory(accountNo, limit);
    }
}