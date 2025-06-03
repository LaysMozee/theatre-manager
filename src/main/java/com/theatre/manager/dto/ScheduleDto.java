package com.theatre.manager.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleDto {
    private Long performanceId;
    private LocalDate date;
    private LocalTime time;

    public ScheduleDto() {}

    public ScheduleDto(Long performanceId, LocalDate date, LocalTime time) {
        this.performanceId = performanceId;
        this.date = date;
        this.time = time;
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
}
