package com.theatre.manager.controller;

import com.theatre.manager.dto.GenreDto;
import com.theatre.manager.dto.PerformanceDto;
import com.theatre.manager.dto.ScheduleDto;
import com.theatre.manager.service.PerformanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/performances")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final JdbcTemplate jdbcTemplate;

    public PerformanceController(PerformanceService performanceService, JdbcTemplate jdbcTemplate) {
        this.performanceService = performanceService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Получить список всех жанров
     * GET /performances/genres
     */
    @GetMapping("/genres")
    public List<GenreDto> getGenres() {
        String sql = "SELECT genre_id, genre_name FROM genre";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new GenreDto(rs.getLong("genre_id"), rs.getString("genre_name")));
    }

    /**
     * Получить список всех спектаклей
     * GET /performances
     */
    @GetMapping
    public List<PerformanceDto> getAllPerformances() {
        String sql = "SELECT performance_id, title, description, duration, genre_id FROM performance";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new PerformanceDto(
                        rs.getLong("performance_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("duration"),
                        rs.getLong("genre_id")
                ));
    }

    /**
     * Создать новый спектакль
     * POST /performances/create
     */
    @PostMapping("/create")
    public ResponseEntity<Long> createPerformance(@RequestBody PerformanceDto dto) {
        try {
            Long id = performanceService.createPerformance(dto);
            return ResponseEntity.ok(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Запланировать спектакль (создать расписание)
     * POST /performances/schedule
     */
    @PostMapping("/schedule")
    public ResponseEntity<String> schedulePerformance(@RequestBody ScheduleDto dto) {
        try {
            performanceService.schedulePerformance(dto);
            return ResponseEntity.ok("Спектакль успешно запланирован");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + e.getMessage());
        }
    }
}
