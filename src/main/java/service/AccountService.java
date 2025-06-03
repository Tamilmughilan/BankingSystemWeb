package service;

import entity.SavingsAccount;
import storage.DataStorage;
import storage.DatabaseStorage;

public class AccountService {
    private final DataStorage dataStorage;

    public AccountService(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    public int createSavingsAccount(int customerId, double initialBalance, int branchID) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }

        if (initialBalance < 100.0) {
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

    public boolean performWithdrawal(int accountNo, double amount) {
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

    public boolean performDeposit(int accountNo, double amount) {
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
}
