package entity;

public class SavingsAccount extends AbstractAccount {
	
	private int branch_id;
	//account specific state
	private static final double MIN_BALANCE = 100.0;
    public SavingsAccount(int accountNo, int customerId, double balance, int branch_id) {
        super(accountNo, customerId, balance, "Savings");
        this.branch_id = branch_id;
    }
    
    //Savings Account specific withdraw check
    @Override
    public boolean canWithdraw(double amount) {
    	return super.canWithdraw(amount) && (balance - amount) >= MIN_BALANCE;
    }
    
    //Single responsibility principle - interest has to be calculated in places where we dont apply interest too.
    public double calculateInterest(double rate) {
        return balance * rate / 100;
    }
    
    //Savings Account specific interest function
    public void applyInterest(double rate) {
    	double interest = calculateInterest(rate);
    	//using abstract method
    	deposit(interest);
    }
    
    public int getBranchId() {
    	return branch_id;
    }
    
    public void setBranchId(int branch_id) {
        this.branch_id = branch_id;
    }

    @Override
    public String toString() {
        return "SavingsAccount: AC " + accountNo + "\nBalance: Rs." + balance +
                "\nCustomer ID: " + customerId + "\nBranch ID: " + branch_id;
    }
}
