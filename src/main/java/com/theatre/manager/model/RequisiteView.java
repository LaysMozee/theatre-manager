package com.theatre.manager.model;

import java.util.Date;

public class RequisiteView {
    private Long id;
    private String title;
    private Date conditionDate;
    private String conditionTypeTitle;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Date getConditionDate() { return conditionDate; }
    public void setConditionDate(Date conditionDate) { this.conditionDate = conditionDate; }

    public String getConditionTypeTitle() { return conditionTypeTitle; }
    public void setConditionTypeTitle(String conditionTypeTitle) { this.conditionTypeTitle = conditionTypeTitle; }
}
