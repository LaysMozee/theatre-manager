package com.theatre.manager.dto;

public class PerformanceDto {
    private Long performanceId;
    private String title;
    private String description;
    private Integer duration;
    private Long genreId;

    public PerformanceDto(Long performanceId, String title, String description, Integer duration, Long genreId) {
        this.performanceId = performanceId;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.genreId = genreId;
    }

    public PerformanceDto() {

    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDuration() {
        return duration;
    }

    public Long getGenreId() {
        return genreId;
    }
    public void setPerformanceId(Long performanceId) {
        this.performanceId = performanceId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

}
