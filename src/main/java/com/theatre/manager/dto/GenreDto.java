package com.theatre.manager.dto;

public class GenreDto {
    private Long genreId;
    private String genreName;

    public GenreDto(Long genreId, String genreName) {
        this.genreId = genreId;
        this.genreName = genreName;
    }
    public Long getGenreId() { return genreId; }
    public String getGenreName() { return genreName; }
}
