package com.theatre.manager.dto;

public class RepertoireWorkerDto {
    private Long id;
    private Long repertoireId;
    private Long workerId;

    public RepertoireWorkerDto() {}

    public RepertoireWorkerDto(Long id, Long repertoireId, Long workerId) {
        this.id = id;
        this.repertoireId = repertoireId;
        this.workerId = workerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRepertoireId() {
        return repertoireId;
    }

    public void setRepertoireId(Long repertoireId) {
        this.repertoireId = repertoireId;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }
}
