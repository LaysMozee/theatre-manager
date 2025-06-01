package com.theatre.manager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Worker_Performance")
public class WorkerPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "worker_performance_id")
    private Long workerPerformanceId;

    @ManyToOne
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;

    // Геттеры и сеттеры

    public Long getWorkerPerformanceId() {
        return workerPerformanceId;
    }

    public void setWorkerPerformanceId(Long workerPerformanceId) {
        this.workerPerformanceId = workerPerformanceId;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }
}
