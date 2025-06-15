package com.theatre.manager.dao;

import com.theatre.manager.model.Repertoire;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Time;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RepertoireDao {

    private final JdbcTemplate jdbcTemplate;

    public RepertoireDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long addRepertoire(Repertoire repertoire) {
        String sql = "INSERT INTO repertoire (date, time, performance_id) VALUES (?, ?, ?) RETURNING repertoire_id";
        return jdbcTemplate.queryForObject(sql,
                new Object[]{
                        Date.valueOf(repertoire.getDate()),
                        Time.valueOf(repertoire.getTime()),
                        repertoire.getPerformanceId()},
                Long.class);
    }

    public void updateRepertoire(Repertoire repertoire) {
        String sql = "UPDATE repertoire SET date = ?, time = ?, performance_id = ? WHERE repertoire_id = ?";
        jdbcTemplate.update(sql,
                Date.valueOf(repertoire.getDate()),
                Time.valueOf(repertoire.getTime()),
                repertoire.getPerformanceId(),
                repertoire.getId());
    }

    public Repertoire getRepertoireById(Long id) {
        String sql = "SELECT repertoire_id, date, time, performance_id FROM repertoire WHERE repertoire_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new RepertoireRowMapper());
    }

    public List<Repertoire> getAllRepertoires() {
        String sql = "SELECT repertoire_id, date, time, performance_id FROM repertoire ORDER BY date, time";
        return jdbcTemplate.query(sql, new RepertoireRowMapper());
    }

    private static class RepertoireRowMapper implements RowMapper<Repertoire> {
        @Override
        public Repertoire mapRow(ResultSet rs, int rowNum) throws SQLException {
            Repertoire r = new Repertoire();
            r.setId(rs.getLong("repertoire_id"));
            r.setDate(rs.getDate("date").toLocalDate());
            r.setTime(rs.getTime("time").toLocalTime());
            r.setPerformanceId(rs.getLong("performance_id"));
            return r;
        }
    }
}
