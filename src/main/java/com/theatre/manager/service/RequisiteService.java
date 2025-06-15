package com.theatre.manager.service;

import com.theatre.manager.dto.RequisiteDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequisiteService {

    private final JdbcTemplate jdbcTemplate;

    public RequisiteService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Возвращает список всех реквизитов с id и названием.
     */
    public List<RequisiteDto> getAllRequisites() {
        String sql = "SELECT requisite_id, title FROM requisite ORDER BY title";
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new RequisiteDto(
                        rs.getLong("requisite_id"),
                        rs.getString("title")
                )
        );
    }
}
