package com.theatre.manager.model;

import java.time.LocalDate;

public class ConditionWithTitle {
    private Long conditionId;
    private Long requisiteId;
    private LocalDate date;
    private int quantity;
    private String comment;
    private int conditionTypeId;
    private String requisiteTitle;
    private String conditionTypeName;

    // Геттеры и сеттеры
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getConditionTypeId() {
        return conditionTypeId;
    }

    public void setConditionTypeId(int conditionTypeId) {
        this.conditionTypeId = conditionTypeId;
    }

    public String getRequisiteTitle() {
        return requisiteTitle;
    }

    public void setRequisiteTitle(String requisiteTitle) {
        this.requisiteTitle = requisiteTitle;
    }

    public String getConditionTypeName() {
        return conditionTypeName;
    }

    public void setConditionTypeName(String conditionTypeName) {
        this.conditionTypeName = conditionTypeName;
    }
}