package com.theatre.manager.model;

import java.time.LocalDate;

public class ConditionWithTitle {
    private Long conditionId;
    private Long requisiteId;
    private String requisiteTitle;
    private Integer conditionTypeId;
    private String conditionTypeName;
    private Integer quantity;
    private String comment;
    private LocalDate date;

    // Конструкторы
    public ConditionWithTitle() {
    }

    public ConditionWithTitle(Long conditionId, Long requisiteId, String requisiteTitle,
                              Integer conditionTypeId, String conditionTypeName,
                              Integer quantity, String comment, LocalDate date) {
        this.conditionId = conditionId;
        this.requisiteId = requisiteId;
        this.requisiteTitle = requisiteTitle;
        this.conditionTypeId = conditionTypeId;
        this.conditionTypeName = conditionTypeName;
        this.quantity = quantity;
        this.comment = comment;
        this.date = date;
    }

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

    public String getRequisiteTitle() {
        return requisiteTitle;
    }

    public void setRequisiteTitle(String requisiteTitle) {
        this.requisiteTitle = requisiteTitle;
    }

    public Integer getConditionTypeId() {
        return conditionTypeId;
    }

    public void setConditionTypeId(Integer conditionTypeId) {
        this.conditionTypeId = conditionTypeId;
    }

    public String getConditionTypeName() {
        return conditionTypeName;
    }

    public void setConditionTypeName(String conditionTypeName) {
        this.conditionTypeName = conditionTypeName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    // toString для удобства отладки
    @Override
    public String toString() {
        return "ConditionWithTitle{" +
                "conditionId=" + conditionId +
                ", requisiteId=" + requisiteId +
                ", requisiteTitle='" + requisiteTitle + '\'' +
                ", conditionTypeId=" + conditionTypeId +
                ", conditionTypeName='" + conditionTypeName + '\'' +
                ", quantity=" + quantity +
                ", comment='" + comment + '\'' +
                ", date=" + date +
                '}';
    }
}