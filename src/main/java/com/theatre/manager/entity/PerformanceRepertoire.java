package com.theatre.manager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "performance_repertoire")
public class PerformanceRepertoire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_repertoire_id")
    private Long id;

    // Связь с Performance (спектакль)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;

    // Связь с Repertoire (афиша - дата и время показа)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repertoire_id", nullable = false)
    private Repertoire repertoire;

    // Геттеры и сеттеры

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    public Repertoire getRepertoire() {
        return repertoire;
    }

    public void setRepertoire(Repertoire repertoire) {
        this.repertoire = repertoire;
    }
}
