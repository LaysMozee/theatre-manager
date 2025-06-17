package com.theatre.manager.dto;

import java.time.LocalDate;

public class ConditionDto {
    private Long conditionId;
    private LocalDate date;
    private Long conditionTypeId;
    private Long requisiteId;
    private int quantity;
    private String comment;

    // Конструктор по умолчанию
    public ConditionDto() {}

    public ConditionDto(Long conditionId, LocalDate date, String typeName, String performance, int quantity, String comment) {
        // Тело конструктора можно реализовать при необходимости использования этих данных
    }

    public ConditionDto(Long conditionId, LocalDate date, Long conditionTypeId,
                        Long requisiteId, int quantity, String comment) {
        this.conditionId = conditionId;
        this.date = date;
        this.conditionTypeId = conditionTypeId;
        this.requisiteId = requisiteId;
        this.quantity = quantity;
        this.comment = comment;
    }

    public Long getConditionId() {
        return conditionId;
    }

    public void setConditionId(Long conditionId) {
        this.conditionId = conditionId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getConditionTypeId() {
        return conditionTypeId;
    }

    public void setConditionTypeId(Long conditionTypeId) {
        this.conditionTypeId = conditionTypeId;
    }

    public Long getRequisiteId() {
        return requisiteId;
    }

    public void setRequisiteId(Long requisiteId) {
        this.requisiteId = requisiteId;
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
}