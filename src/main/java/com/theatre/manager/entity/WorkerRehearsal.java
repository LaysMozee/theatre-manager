package com.theatre.manager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Worker_Rehearsal")
public class WorkerRehearsal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "worker_rehearsal_id")
    private Long workerRehearsalId;

    @ManyToOne
    @JoinColumn(name = "rehearsal_id", nullable = false)
    private Rehearsal rehearsal;

    @ManyToOne
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    // Геттеры и сеттеры

    public Long getWorkerRehearsalId() {
        return workerRehearsalId;
    }

    public void setWorkerRehearsalId(Long workerRehearsalId) {
        this.workerRehearsalId = workerRehearsalId;
    }

    public Rehearsal getRehearsal() {
        return rehearsal;
    }

    public void setRehearsal(Rehearsal rehearsal) {
        this.rehearsal = rehearsal;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }
}
