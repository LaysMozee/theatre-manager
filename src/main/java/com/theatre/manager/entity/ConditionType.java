package com.theatre.manager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "condition_type")
public class ConditionType {

    @Id
    @Column(name = "condition_type_id")
    private Long conditionTypeId;

    @Column(name = "condition_type_name", nullable = false)
    private String conditionTypeName;

    public ConditionType() {}

    // Геттеры и сеттеры
    public Long getConditionTypeId() { return conditionTypeId; }
    public void setConditionTypeId(Long conditionTypeId) { this.conditionTypeId = conditionTypeId; }

    public String getConditionTypeName() { return conditionTypeName; }
    public void setConditionTypeName(String conditionTypeName) { this.conditionTypeName = conditionTypeName; }
}
