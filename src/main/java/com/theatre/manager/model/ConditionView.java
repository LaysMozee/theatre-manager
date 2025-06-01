package com.theatre.manager.model;

import java.util.Date;

public class ConditionView {
    private Long conditionId;
    private Long requisiteId;
    private String title;
    private String conditionTypeTitle;
    private Date date;

    public Long getConditionId() {
        return conditionId;
    }
    public void setConditionId(Long conditionId) {
        this.conditionId = conditionId;
    }

    public Long getRequisiteId() {
        return requisiteId;
    }
    public void setRequisiteId(Long requisiteId) {
        this.requisiteId = requisiteId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getConditionTypeTitle() {
        return conditionTypeTitle;
    }
    public void setConditionTypeTitle(String conditionTypeTitle) {
        this.conditionTypeTitle = conditionTypeTitle;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
}
