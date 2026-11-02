// Transaction.java
package com.example.pos_disoftwa.models;

public class Transaction {
    private int id;
    private String transactionCode;
    private String item; // JSON data
    private double totalTransaction;
    private String createdAt;

    public Transaction() {}

    public Transaction(int id, String transactionCode, String item, double totalTransaction, String createdAt) {
        this.id = id;
        this.transactionCode = transactionCode;
        this.item = item;
        this.totalTransaction = totalTransaction;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public double getTotalTransaction() {
        return totalTransaction;
    }

    public void setTotalTransaction(double totalTransaction) {
        this.totalTransaction = totalTransaction;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
