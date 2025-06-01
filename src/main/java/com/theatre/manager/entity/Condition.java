package com.theatre.manager.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "condition")
public class Condition {

    @Id
    @Column(name = "condition_id")
    private Long conditionId;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "requisite_id", nullable = false)
    private Requisite requisite;

    @ManyToOne
    @JoinColumn(name = "condition_type_id", nullable = false)
    private ConditionType conditionType;

    public Condition() {}

    // Геттеры и сеттеры
    public Long getConditionId() { return conditionId; }
    public void setConditionId(Long conditionId) { this.conditionId = conditionId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Requisite getRequisite() { return requisite; }
    public void setRequisite(Requisite requisite) { this.requisite = requisite; }

    public ConditionType getConditionType() { return conditionType; }
    public void setConditionType(ConditionType conditionType) { this.conditionType = conditionType; }
}
