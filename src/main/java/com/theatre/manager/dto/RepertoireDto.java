package com.theatre.manager.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class RepertoireDto {
    private Long repertoireId;
    private Long performanceId;
    private LocalDate date;
    private LocalTime time;
    private String performanceTitle;  // добавлено

    public RepertoireDto() {}

    public RepertoireDto(Long repertoireId, Long performanceId, LocalDate date, LocalTime time, String performanceTitle) {
        this.repertoireId = repertoireId;
        this.performanceId = performanceId;
        this.date = date;
        this.time = time;
        this.performanceTitle = performanceTitle;
    }

    public Long getRepertoireId() {
        return repertoireId;
    }

    public void setRepertoireId(Long repertoireId) {
        this.repertoireId = repertoireId;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public void setPerformanceId(Long performanceId) {
        this.performanceId = performanceId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getPerformanceTitle() {
        return performanceTitle;
    }

    public void setPerformanceTitle(String performanceTitle) {
        this.performanceTitle = performanceTitle;
    }
}
