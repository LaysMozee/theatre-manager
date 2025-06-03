package com.theatre.manager.dto;

public class WorkerDto {
    private Long workerId;
    private String fio;

    public WorkerDto() { }

    public WorkerDto(Long workerId, String fio) {
        this.workerId = workerId;
        this.fio = fio;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }
}
