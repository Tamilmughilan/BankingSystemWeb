package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a transaction log entry in the banking system.
 * Stores details of deposit/withdrawal transactions, including metadata and audit info.
 * 
 * <p>This class tracks balances before and after the transaction, the user who initiated it,
 * and the status of the operation.</p>
 * 
 * @author TAMIL
 */
public class TransactionLog {

    /**
     * Enum representing the type of transaction.
     */
    public enum TransactionType {
        DEPOSIT, WITHDRAWAL
    }

    /**
     * Enum representing the status of a transaction.
     */
    public enum TransactionStatus {
        SUCCESS, FAILED
    }

    /**
     * Enum representing the user type who initiated the transaction.
     */
    public enum UserType {
        CUSTOMER, EMPLOYEE, MANAGER
    }

    private Long transactionId;
    private int accountNo;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private LocalDateTime transactionDate;
    private TransactionStatus status;
    private String description;
    private Integer createdByUserId;
    private UserType createdByUserType;

    /**
     * Default no argument constructor.
     */
    public TransactionLog() {}

    /**
     * Constructs a basic transaction log entry.
     * 
     * @param accountNo       the account number involved
     * @param transactionType type of transaction (deposit or withdrawal)
     * @param amount          the transaction amount
     * @param balanceBefore   balance before transaction
     * @param balanceAfter    balance after transaction
     * @param status          transaction status (success or failed)
     */
    public TransactionLog(int accountNo, TransactionType transactionType, BigDecimal amount,
                          BigDecimal balanceBefore, BigDecimal balanceAfter, TransactionStatus status) {
        this.accountNo = accountNo;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.status = status;
        this.transactionDate = LocalDateTime.now();
    }

    /**
     * Constructs a transaction log entry with additional audit fields.
     * 
     * @param accountNo         the account number involved
     * @param transactionType   type of transaction
     * @param amount            the transaction amount
     * @param balanceBefore     balance before transaction
     * @param balanceAfter      balance after transaction
     * @param status            transaction status
     * @param description       optional transaction description
     * @param createdByUserId   user ID of the person who created the transaction
     * @param createdByUserType type of user who initiated the transaction
     */
    public TransactionLog(int accountNo, TransactionType transactionType, BigDecimal amount,
                          BigDecimal balanceBefore, BigDecimal balanceAfter, TransactionStatus status,
                          String description, Integer createdByUserId, UserType createdByUserType) {
        this(accountNo, transactionType, amount, balanceBefore, balanceAfter, status);
        this.description = description;
        this.createdByUserId = createdByUserId;
        this.createdByUserType = createdByUserType;
    }

    // Getters and Setters

    /**
     * Gets the transaction ID.
     * 
     * @return the transaction ID
     */
    public Long getTransactionId() {
        return transactionId;
    }

    /**
     * Sets the transaction ID.
     * 
     * @param transactionId the transaction ID
     */
    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Gets the account number involved in the transaction.
     * 
     * @return the account number
     */
    public int getAccountNo() {
        return accountNo;
    }
    
    /**
     * Sets the Account no.
     * 
     * @param accountNo the accountNo
     */
    public void setAccountNo(int accountNo) {
        this.accountNo = accountNo;
    }

    /**
     * Gets the transaction type.
     * 
     * @return the transaction type
     */
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    /**
     * Sets the transaction Type.
     * 
     * @param transactionType the transaction Type
     */

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * Gets the transaction amount.
     * 
     * @return the amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Sets the Amount.
     * 
     * @param amount the amount to be set
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Gets the balance before the transaction.
     * 
     * @return the balance before transaction
     */
    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    /**
     * Gets the balance after the transaction.
     * 
     * @return the balance after transaction
     */
    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }
    
    /**
     * Sets the Balance after transaction.
     * 
     * @param balanceAfter the balanceAfter transaction
     */
    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    /**
     * Gets the date and time of the transaction.
     * 
     * @return the transaction date
     */
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    /**
     * Gets the transaction status.
     * 
     * @return the status
     */
    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    /**
     * Gets the optional transaction description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the user ID of the person who initiated the transaction.
     * 
     * @return the creator user ID
     */
    public Integer getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Integer createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    /**
     * Gets the user type of the person who created the transaction.
     * 
     * @return the user type
     */
    public UserType getCreatedByUserType() {
        return createdByUserType;
    }

    public void setCreatedByUserType(UserType createdByUserType) {
        this.createdByUserType = createdByUserType;
    }

    @Override
    public String toString() {
        return String.format("TransactionLog{id=%d, accountNo=%d, type=%s, amount=%.2f, " +
                             "balanceBefore=%.2f, balanceAfter=%.2f, date=%s, status=%s}",
                             transactionId, accountNo, transactionType, amount,
                             balanceBefore, balanceAfter, transactionDate, status);
    }
}
