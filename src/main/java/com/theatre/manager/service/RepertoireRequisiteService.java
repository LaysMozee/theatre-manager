package com.theatre.manager.service;

import com.theatre.manager.dto.RepertoireRequisiteDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepertoireRequisiteService {

    private final JdbcTemplate jdbcTemplate;

    public RepertoireRequisiteService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Получить реквизиты по id репертуара
    public List<RepertoireRequisiteDto> findByRepertoireId(Long repertoireId) {
        String sql = "SELECT id, repertoire_id, requisite_id, quantity FROM repertoire_requisite WHERE repertoire_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new RepertoireRequisiteDto(
        ), repertoireId);
    }

    // Добавить реквизит к репертуару
    public void addRequisiteToRepertoire(Long repertoireId, Long requisiteId, int quantity) {
        String sql = "INSERT INTO repertoire_requisite (repertoire_id, requisite_id, quantity) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, repertoireId, requisiteId, quantity);
    }

    // Удалить реквизиты из репертуара
    public void deleteRequisitesByRepertoireId(Long repertoireId) {
        String sql = "DELETE FROM repertoire_requisite WHERE repertoire_id = ?";
        jdbcTemplate.update(sql, repertoireId);
    }
}
