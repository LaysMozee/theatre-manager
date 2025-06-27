package com.theatre.manager.controller;

import com.theatre.manager.dto.PerformanceDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Controller
public class PerformanceUIController {

    private final JdbcTemplate jdbcTemplate;

    public PerformanceUIController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Страница со списком спектаклей (доступна всем)
    @GetMapping("/performances/view")
    public String showPerformancesPage(Model model, HttpSession session) {
        // Получаем жанры
        List<GenreDto> genres = jdbcTemplate.query(
                "SELECT genre_id, genre_name FROM genre",
                (rs, rowNum) -> new GenreDto(rs.getLong("genre_id"), rs.getString("genre_name"))
        );

        // Получаем спектакли с жанрами
        List<PerformanceView> performances = jdbcTemplate.query(
                "SELECT p.performance_id, p.title, p.description, p.duration, p.genre_id, g.genre_name " +
                        "FROM performance p JOIN genre g ON p.genre_id = g.genre_id",
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
        model.addAttribute("canAdd", isAdminOrDirector(session));
        return "performances";
    }

    // Форма добавления спектакля (только для админов и директоров)
    @GetMapping("/performances/add")
    public String showAddPerformanceForm(Model model, HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/performances/view?error=accessDenied";
        }

        model.addAttribute("genres", jdbcTemplate.query(
                "SELECT genre_id, genre_name FROM genre",
                (rs, rowNum) -> new GenreDto(rs.getLong("genre_id"), rs.getString("genre_name"))
        ));

        return "performance-form";
    }

    @PostMapping("/performances/create")
    @ResponseBody
    public Map<String, Object> createPerformanceJson(@RequestBody PerformanceDto dto, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (!isAdminOrDirector(session)) {
            response.put("status", "error");
            response.put("message", "Доступ запрещен");
            return response;
        }

        try {
            jdbcTemplate.update(
                    "INSERT INTO performance (title, description, duration, genre_id) VALUES (?, ?, ?, ?)",
                    dto.getTitle(), dto.getDescription(), dto.getDuration(), dto.getGenreId()
            );
            Long newId = jdbcTemplate.queryForObject(
                    "SELECT currval(pg_get_serial_sequence('performance', 'performance_id'))", Long.class);
            response.put("id", newId);
            response.put("status", "success");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("id", -1);
            response.put("status", "error");
        }
        return response;
    }

    @GetMapping("/performances/edit/{id}")
    public String editPerformanceForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/performances/view?error=accessDenied";
        }

        PerformanceDto performance = jdbcTemplate.queryForObject(
                "SELECT performance_id, title, description, duration, genre_id FROM performance WHERE performance_id = ?",
                new Object[]{id},
                (rs, rowNum) -> new PerformanceDto(
                        rs.getLong("performance_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("duration"),
                        rs.getLong("genre_id")
                )
        );
        model.addAttribute("performance", performance);

        List<GenreDto> genres = jdbcTemplate.query(
                "SELECT genre_id, genre_name FROM genre",
                (rs, rowNum) -> new GenreDto(rs.getLong("genre_id"), rs.getString("genre_name"))
        );
        model.addAttribute("genres", genres);

        return "performance-edit-form";
    }

    @PostMapping("/performances/update")
    public String updatePerformance(@ModelAttribute("performance") PerformanceDto performance, HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/performances/view?error=accessDenied";
        }

        int rows = jdbcTemplate.update(
                "UPDATE performance SET title = ?, description = ?, duration = ?, genre_id = ? WHERE performance_id = ?",
                performance.getTitle(),
                performance.getDescription(),
                performance.getDuration(),
                performance.getGenreId(),
                performance.getPerformanceId()
        );

        return "redirect:/performances/view";
    }

    @DeleteMapping("/performances/delete/{id}")
    @ResponseBody
    public Map<String, String> deletePerformance(@PathVariable Long id, HttpSession session) {
        Map<String, String> response = new HashMap<>();

        if (!isAdminOrDirector(session)) {
            response.put("status", "error");
            response.put("message", "Доступ запрещен");
            return response;
        }

        try {
            jdbcTemplate.update("DELETE FROM performance WHERE performance_id = ?", id);
            response.put("status", "success");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return response;
    }

    private boolean isAdminOrDirector(HttpSession session) {
        String role = (String) session.getAttribute("workerRole");
        return "admin".equals(role) || "director".equals(role);
    }

    // Вспомогательные классы DTO для отображения
    public static class PerformanceView {
        public Long performanceId;
        public String title;
        public String description;
        public int duration;
        public Long genreId;
        public String genreName;

        public PerformanceView(Long performanceId, String title, String description, int duration, Long genreId, String genreName) {
            this.performanceId = performanceId;
            this.title = title;
            this.description = description;
            this.duration = duration;
            this.genreId = genreId;
            this.genreName = genreName;
        }
    }

    public static class GenreDto {
        public Long genreId;
        public String genreName;

        public GenreDto(Long genreId, String genreName) {
            this.genreId = genreId;
            this.genreName = genreName;
        }
    }
}