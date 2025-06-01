package com.theatre.manager.model;

import java.util.Date;

public class ConditionWithTitle {
    private Long conditionId;
    private Long requisiteId;
    private String title;
    private String conditionTypeTitle;
    private int quantity;
    private Date date;
    private String comment;

    // Геттеры и сеттеры для всех полей
    public Long getConditionId() {
        return conditionId;
    }

    public void setConditionId(Long conditionId) {
        this.conditionId = conditionId;
    }

    public Long getRequisiteId() {
        return requisiteId;
    }

    public void setRequisiteId(Long requisiteId) {
        this.requisiteId = requisiteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getConditionTypeTitle() {
        return conditionTypeTitle;
    }

    public void setConditionTypeTitle(String conditionTypeTitle) {
        this.conditionTypeTitle = conditionTypeTitle;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}