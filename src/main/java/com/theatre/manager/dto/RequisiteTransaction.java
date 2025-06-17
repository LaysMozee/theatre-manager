package com.theatre.manager.dto;

public class RequisiteTransaction {
    private String title;
    private int quantity;
    private Object requisiteId;

    public RequisiteTransaction() {
    }

    public RequisiteTransaction(long aLong, String title, int quantity) {
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

    public Object getRequisiteId() {
        return requisiteId;
    }

    public void setRequisiteId(Object requisiteId) {
        this.requisiteId = requisiteId;
    }
}
