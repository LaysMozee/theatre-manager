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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RehearsalService {

    private final JdbcTemplate jdbcTemplate;

    public RehearsalService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Long addRehearsal(LocalDate date,
                             LocalTime time,
                             String room,
                             String comment,
                             List<Long> actorIds) {
        // вставка в таблицу rehearsal, возвращаем newId
        String sqlInsert =
                "INSERT INTO rehearsal(date, time, rehearsal_room, comment) " +
                        "VALUES (?, ?, ?, ?) RETURNING rehearsal_id";
        Long newId = jdbcTemplate.queryForObject(
                sqlInsert,
                new Object[]{date, time, room, comment},
                new int[]{Types.DATE, Types.TIME, Types.VARCHAR, Types.VARCHAR},
                Long.class
        );
        // вставляем связи с актёрами — только actorIds
        String sqlLink = "INSERT INTO rehearsal_worker(rehearsal_id, worker_id) VALUES (?, ?)";
        for (Long aId : actorIds) {
            jdbcTemplate.update(sqlLink, newId, aId);
        }
        return newId;
    }
    public RehearsalDto getRehearsalById(Long rehearsalId) {
        String sql =
                "SELECT r.rehearsal_id, r.date, r.time, r.rehearsal_room, r.comment, " +
                        "       w.worker_id, w.fio " +
                        "FROM rehearsal r " +
                        "LEFT JOIN rehearsal_worker rw ON r.rehearsal_id = rw.rehearsal_id " +
                        "LEFT JOIN worker w ON rw.worker_id = w.worker_id " +
                        "WHERE r.rehearsal_id = ?";
        List<RehearsalDto> list = jdbcTemplate.query(
                sql,
                new RehearsalResultSetExtractor(),
                rehearsalId
        );
        return list.isEmpty() ? null : list.get(0);
    }
    public List<RehearsalDto> getAllRehearsals() {
        String sql =
                "SELECT r.rehearsal_id, r.date, r.time, r.rehearsal_room, r.comment, " +
                        "       w.worker_id, w.fio " +
                        "FROM rehearsal r " +
                        "LEFT JOIN rehearsal_worker rw ON r.rehearsal_id = rw.rehearsal_id " +
                        "LEFT JOIN worker w ON rw.worker_id = w.worker_id " +
                        "ORDER BY r.date, r.time";
        return jdbcTemplate.query(sql, new RehearsalResultSetExtractor());
    }
    public List<WorkerDto> getAllActors() {
        String sql = "SELECT worker_id, fio FROM worker WHERE post = 'Актёр' ORDER BY fio";
        return jdbcTemplate.query(sql, new WorkerRowMapper());
    }

    public List<WorkerDto> getAllWorkers() {
        String sql = "SELECT worker_id, fio FROM worker ORDER BY fio";
        return jdbcTemplate.query(sql, new WorkerRowMapper());
    }

    @Transactional
    public void updateRehearsal(Long rehearsalId,
                                LocalDate date,
                                LocalTime time,
                                String room,
                                String comment,
                                List<Long> actorIds) {
        // Обновляем основные поля репетиции
        String sqlUpdate =
                "UPDATE rehearsal SET date = ?, time = ?, rehearsal_room = ?, comment = ? " +
                        "WHERE rehearsal_id = ?";
        jdbcTemplate.update(sqlUpdate, date, time, room, comment, rehearsalId);

        // Удаляем старые связи
        jdbcTemplate.update(
                "DELETE FROM rehearsal_worker WHERE rehearsal_id = ?",
                rehearsalId
        );

        // Вставляем новые связи — только из actorIds
        if (actorIds != null) {
            String sqlLink = "INSERT INTO rehearsal_worker(rehearsal_id, worker_id) VALUES (?, ?)";
            for (Long aId : actorIds) {
                jdbcTemplate.update(sqlLink, rehearsalId, aId);
            }
        }
    }


    @Transactional
    public void deleteRehearsal(Long rehearsalId) {
        jdbcTemplate.update(
                "DELETE FROM rehearsal_worker WHERE rehearsal_id = ?",
                rehearsalId
        );
        jdbcTemplate.update(
                "DELETE FROM rehearsal WHERE rehearsal_id = ?",
                rehearsalId
        );
    }
    /***
     * Вспомогательный метод: из переданного списка workerIds оставляет только те ID, которые
     * действительно есть в таблице worker, и вставляет их в rehearsal_worker.
     */
    private void insertRehearsalWorkers(Long rehearsalId, List<Long> workerIds) {
        if (workerIds == null || workerIds.isEmpty()) {
            return;
        }

        // Формируем SQL вида "... WHERE worker_id IN (?,?,...)" только для переданных ID
        String inClause = workerIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));
        String checkSql = "SELECT worker_id FROM worker WHERE worker_id IN (" + inClause + ")";

        // Параметры тем же порядком, что и inClause
        Object[] params = workerIds.toArray();

        // Получаем из БД только те ID, которые реально есть
        List<Long> validIds = jdbcTemplate.query(
                checkSql,
                params,
                (rs, rowNum) -> rs.getLong("worker_id")
        );

        if (validIds.isEmpty()) {
            return;
        }

        // Теперь вставляем связи только для «валидных» ID
        String insertSql = "INSERT INTO rehearsal_worker (rehearsal_id, worker_id) VALUES (?, ?)";
        for (Long validId : validIds) {
            jdbcTemplate.update(insertSql, rehearsalId, validId);
        }
    }
}
