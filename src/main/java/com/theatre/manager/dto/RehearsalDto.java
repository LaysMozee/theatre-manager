package com.theatre.manager.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RehearsalDto {
    private Long rehearsalId;
    private LocalDate date;
    private LocalTime time;
    private String rehearsalRoom;
    private String comment;


    private List<WorkerDto> workers = new ArrayList<>();

    public RehearsalDto() { }

    public Long getRehearsalId() {
        return rehearsalId;
    }

    public void setRehearsalId(Long rehearsalId) {
        this.rehearsalId = rehearsalId;
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

    public String getRehearsalRoom() {
        return rehearsalRoom;
    }

    public void setRehearsalRoom(String rehearsalRoom) {
        this.rehearsalRoom = rehearsalRoom;
    }

    public List<WorkerDto> getWorkers() {
        return workers;
    }

    public void setWorkers(List<WorkerDto> workers) {
        this.workers = workers;
    }

    public void addWorker(WorkerDto worker) {
        this.workers.add(worker);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
