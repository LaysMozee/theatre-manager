package com.theatre.manager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Performance_Requisite")
public class PerformanceRequisite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_requisite_id")
    private Long performanceRequisiteId;

    @ManyToOne
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;

    @ManyToOne
    @JoinColumn(name = "requisite_id", nullable = false)
    private Requisite requisite;

    // Геттеры и сеттеры

    public Long getPerformanceRequisiteId() {
        return performanceRequisiteId;
    }

    public void setPerformanceRequisiteId(Long performanceRequisiteId) {
        this.performanceRequisiteId = performanceRequisiteId;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    public Requisite getRequisite() {
        return requisite;
    }

    public void setRequisite(Requisite requisite) {
        this.requisite = requisite;
    }
}
