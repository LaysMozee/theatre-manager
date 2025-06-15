package com.theatre.manager.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RepertoireLinkDao {

    private final JdbcTemplate jdbcTemplate;

    public RepertoireLinkDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addRepertoireRequisite(int repertoireId, int requisiteId, int quantity) {
        String sql = "INSERT INTO repertoire_requisite (repertoire_id, requisite_id, quantity) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, repertoireId, requisiteId, quantity);
    }

    public void addRepertoireWorker(int repertoireId, int workerId) {
        String sql = "INSERT INTO repertoire_worker (repertoire_id, worker_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, repertoireId, workerId);
    }

    public void deleteRepertoireRequisites(int repertoireId) {
        String sql = "DELETE FROM repertoire_requisite WHERE repertoire_id = ?";
        jdbcTemplate.update(sql, repertoireId);
    }

    public void deleteRepertoireWorkers(int repertoireId) {
        String sql = "DELETE FROM repertoire_worker WHERE repertoire_id = ?";
        jdbcTemplate.update(sql, repertoireId);
    }
}
