package com.theatre.manager.dto;

public class RequisiteTransaction {
    private String title;
    private int quantity;

    public RequisiteTransaction() {
    }

    public RequisiteTransaction(String title, int quantity) {
        this.title = title;
        this.quantity = quantity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
