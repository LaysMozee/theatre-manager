package com.theatre.manager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "condition_type")
public class ConditionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer conditionTypeId;

    private String conditionTypeName;

    public Integer getConditionTypeId() {
        return conditionTypeId;
    }

    public void setConditionTypeId(Integer conditionTypeId) {
        this.conditionTypeId = conditionTypeId;
    }

    public String getConditionTypeName() {
        return conditionTypeName;
    }

    public void setConditionTypeName(String conditionTypeName) {
        this.conditionTypeName = conditionTypeName;
    }

    @Override
    public String toString() {
        return conditionTypeName;
    }
}
