package com.theatre.manager.service;

import com.theatre.manager.dto.RehearsalDto;
import com.theatre.manager.dto.WorkerDto;
import com.theatre.manager.mapper.RehearsalResultSetExtractor;
import com.theatre.manager.mapper.WorkerRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class RehearsalService {

    private final JdbcTemplate jdbcTemplate;

    public RehearsalService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Long addRehearsal(LocalDate date, LocalTime time, String room, List<Long> workerIds) {
        // Вставляем репетицию и получаем сгенерированный rehearsal_id
        String sqlInsertRehearsal =
                "INSERT INTO rehearsal(date, time, rehearsal_room) VALUES (?, ?, ?) RETURNING rehearsal_id";
        Long rehearsalId = jdbcTemplate.queryForObject(
                sqlInsertRehearsal,
                new Object[]{date, time, room},
                new int[]{Types.DATE, Types.TIME, Types.VARCHAR},
                Long.class
        );

        // Вставляем связи репетиции с работниками
        String sqlInsertLink = "INSERT INTO rehearsal_worker(rehearsal_id, worker_id) VALUES (?, ?)";
        for (Long wId : workerIds) {
            jdbcTemplate.update(sqlInsertLink, rehearsalId, wId);
        }
        return rehearsalId;
    }

    public List<RehearsalDto> getAllRehearsals() {
        String sql = "SELECT r.rehearsal_id, r.date, r.time, r.rehearsal_room, " +
                "w.worker_id, w.fio " +
                "FROM rehearsal r " +
                "LEFT JOIN rehearsal_worker rw ON r.rehearsal_id = rw.rehearsal_id " +
                "LEFT JOIN worker w ON rw.worker_id = w.worker_id " +
                "ORDER BY r.date, r.time";
        return jdbcTemplate.query(sql, new RehearsalResultSetExtractor());
    }

    public List<WorkerDto> getAllWorkers() {
        String sql = "SELECT worker_id, fio FROM worker ORDER BY fio";
        return jdbcTemplate.query(sql, new WorkerRowMapper());
    }
}
