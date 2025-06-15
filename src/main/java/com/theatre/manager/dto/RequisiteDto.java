package com.theatre.manager.dto;

public class RequisiteDto {
    private Long id;
    private String name;

    public RequisiteDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
