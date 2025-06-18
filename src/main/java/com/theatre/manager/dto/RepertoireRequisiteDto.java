package com.theatre.manager.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class RepertoireRequisiteDto {
    private String requisiteTitle;
    private String performanceTitle;
    private LocalDate date;
    private int quantity;
    private LocalTime time;

    public RepertoireRequisiteDto() {
    }

    // getters and setters
    public String getRequisiteTitle() {
        return requisiteTitle;
    }

    public void setRequisiteTitle(String requisiteTitle) {
        this.requisiteTitle = requisiteTitle;
    }

    public String getPerformanceTitle() {
        return performanceTitle;
    }

    public void setPerformanceTitle(String performanceTitle) {
        this.performanceTitle = performanceTitle;
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

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public LocalTime getTime() {
        return time;
    }
}