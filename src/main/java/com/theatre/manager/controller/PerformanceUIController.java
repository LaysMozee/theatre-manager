package com.theatre.manager.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PerformanceUIController {

    private final JdbcTemplate jdbcTemplate;

    public PerformanceUIController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Показывает HTML-страницу со списком спектаклей и фильтром по жанрам.
     * URL: GET /performances/view
     */
    @GetMapping("/performances/view")
    public String showPerformancesPage(Model model) {
        // 1. Получаем все жанры (genre_id + genre_name)
        List<GenreDto> genres = jdbcTemplate.query(
                "SELECT genre_id, genre_name FROM genre",
                (rs, rowNum) -> new GenreDto(rs.getLong("genre_id"), rs.getString("genre_name"))
        );

        // 2. Получаем все спектакли вместе с названием жанра
        List<PerformanceView> performances = jdbcTemplate.query(
                "SELECT p.performance_id, p.title, p.description, p.duration, p.genre_id, g.genre_name " +
                        "FROM performance p " +
                        "JOIN genre g ON p.genre_id = g.genre_id",
                (rs, rowNum) -> new PerformanceView(
                        rs.getLong("performance_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("duration"),
                        rs.getLong("genre_id"),
                        rs.getString("genre_name")
                )
        );

        model.addAttribute("genres", genres);
        model.addAttribute("performances", performances);
        // Передаём в шаблон значения, чтобы Thymeleaf подставил их в HTML
        return "performances";  // имя Thymeleaf-шаблона: src/main/resources/templates/performances.html
    }

    // Вложенный статический класс для отображения спектаклей вместе с genre_name
    public static class PerformanceView {
        public Long id;
        public String title;
        public String description;
        public int duration;
        public Long genreId;
        public String genreName;

        public PerformanceView(Long id, String title, String description, int duration, Long genreId, String genreName) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.duration = duration;
            this.genreId = genreId;
            this.genreName = genreName;
        }
    }

    // DTO для жанра (если ещё не создан)
    public static class GenreDto {
        public Long genreId;
        public String genreName;

        public GenreDto(Long genreId, String genreName) {
            this.genreId = genreId;
            this.genreName = genreName;
        }
    }
}
