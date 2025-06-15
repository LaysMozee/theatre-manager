package com.theatre.manager.service;

import com.theatre.manager.dto.RepertoireDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepertoireService {

    private final JdbcTemplate jdbcTemplate;

    public RepertoireService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Получить все записи репертуара
    public List<RepertoireDto> findAll() {
        String sql = "SELECT r.repertoire_id, r.performance_id, r.date, r.time, p.title AS performance_title " +
                "FROM repertoire r " +
                "JOIN performance p ON r.performance_id = p.performance_id " +
                "ORDER BY r.date, r.time";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new RepertoireDto(
                rs.getLong("repertoire_id"),
                rs.getLong("performance_id"),
                rs.getDate("date").toLocalDate(),
                rs.getTime("time").toLocalTime(),
                rs.getString("performance_title")  // тут название
        ));
    }

    // Создать запись в репертуаре
    public Long createRepertoire(RepertoireDto dto) {
        String sql = "INSERT INTO repertoire (performance_id, date, time) VALUES (?, ?, ?) RETURNING repertoire_id";
        return jdbcTemplate.queryForObject(sql, Long.class, dto.getPerformanceId(), dto.getDate(), dto.getTime());
    }

    // Обновить запись в репертуаре
    public void updateRepertoire(RepertoireDto dto) {
        String sql = "UPDATE repertoire SET performance_id = ?, date = ?, time = ? WHERE repertoire_id = ?";
        jdbcTemplate.update(sql, dto.getPerformanceId(), dto.getDate(), dto.getTime(), dto.getRepertoireId());
    }

    // Удалить запись из репертуара
    public void deleteRepertoire(Long id) {
        String sql = "DELETE FROM repertoire WHERE repertoire_id = ?";
        jdbcTemplate.update(sql, id);
    }
}
