package com.theatre.manager.service;

import com.theatre.manager.dto.RepertoireWorkerDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepertoireWorkerService {

    private final JdbcTemplate jdbcTemplate;

    public RepertoireWorkerService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Получить сотрудников по id репертуара
    public List<RepertoireWorkerDto> findByRepertoireId(Long repertoireId) {
        String sql = "SELECT id, repertoire_id, worker_id FROM repertoire_worker WHERE repertoire_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new RepertoireWorkerDto(
                rs.getLong("id"),
                rs.getLong("repertoire_id"),
                rs.getLong("worker_id")
        ), repertoireId);
    }

    // Добавить сотрудника к репертуару
    public void addWorkerToRepertoire(Long repertoireId, Long workerId) {
        String sql = "INSERT INTO repertoire_worker (repertoire_id, worker_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, repertoireId, workerId);
    }

    // Удалить сотрудников из репертуара
    public void deleteWorkersByRepertoireId(Long repertoireId) {
        String sql = "DELETE FROM repertoire_worker WHERE repertoire_id = ?";
        jdbcTemplate.update(sql, repertoireId);
    }
}
