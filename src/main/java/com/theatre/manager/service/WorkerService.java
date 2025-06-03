package com.theatre.manager.service;

import com.theatre.manager.dto.WorkerDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WorkerService {
    private final JdbcTemplate jdbcTemplate;

    public WorkerService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Возвращает список WorkerDto тех сотрудников, у которых поле post = переданное значение.
     */
    public List<WorkerDto> getWorkersByPostDto(String post) {
        String sql = "SELECT worker_id, fio FROM worker WHERE post = ? ORDER BY fio";
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new WorkerDto(
                        rs.getLong("worker_id"),
                        rs.getString("fio")
                ),
                post
        );
    }
}
