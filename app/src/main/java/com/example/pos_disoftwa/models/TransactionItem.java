package com.example.pos_disoftwa.models;

public class TransactionItem {
    private int productId;
    private String productName;
    private int quantity;
    private double unitTotal;
    private double totalPrice;

    public TransactionItem() {}

    public TransactionItem(String productName, int quantity, double unitTotal, double totalPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitTotal = unitTotal;
        this.totalPrice = totalPrice;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        recalculateTotal();
    }

    public double getUnitTotal() {
        return unitTotal;
    }

    public void setUnitTotal(double unitTotal) {
        this.unitTotal = unitTotal;
        recalculateTotal();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    private void recalculateTotal() {
        this.totalPrice = this.unitTotal * this.quantity;
    }
}
