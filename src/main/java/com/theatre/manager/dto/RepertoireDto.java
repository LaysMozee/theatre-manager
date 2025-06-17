package com.theatre.manager.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class RepertoireDto {
    private Long repertoireId;
    private Long performanceId;
    private LocalDate date;
    private LocalTime time;
    private String performanceTitle;
    private List<String> workerNames;
    private List<RequisiteTransaction> requisites;

    public RepertoireDto() {
    }

    public RepertoireDto(Long repertoireId, Long performanceId, LocalDate date, LocalTime time, String performanceTitle,
                         List<String> workerNames, List<RequisiteTransaction> requisites) {
        this.repertoireId = repertoireId;
        this.performanceId = performanceId;
        this.date = date;
        this.time = time;
        this.performanceTitle = performanceTitle;
        this.workerNames = workerNames;
        this.requisites = requisites;
    }

    public RepertoireDto(long repertoireId, long performanceId, LocalDate date, LocalTime time, String performanceTitle) {
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

    public List<String> getWorkerNames() {
        return workerNames;
    }

    public void setWorkerNames(List<String> workerNames) {
        this.workerNames = workerNames;
    }

    public List<RequisiteTransaction> getRequisites() {
        return requisites;
    }

    public void setRequisites(List<RequisiteTransaction> requisites) {
        this.requisites = requisites;
    }
}
