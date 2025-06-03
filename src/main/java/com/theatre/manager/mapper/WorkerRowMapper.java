package com.theatre.manager.mapper;

import com.theatre.manager.dto.WorkerDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WorkerRowMapper implements RowMapper<WorkerDto> {
    @Override
    public WorkerDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        WorkerDto w = new WorkerDto();
        w.setWorkerId(rs.getLong("worker_id"));
        w.setFio(rs.getString("fio"));
        return w;
    }
}
