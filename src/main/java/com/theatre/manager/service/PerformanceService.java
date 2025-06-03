package com.theatre.manager.service;

import com.theatre.manager.dto.PerformanceDto;
import com.theatre.manager.dto.ScheduleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PerformanceService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Long createPerformance(PerformanceDto dto) {
        return jdbcTemplate.queryForObject(
                "SELECT create_performance(?, ?, ?, ?)",
                Long.class,
                dto.getTitle(),
                dto.getDescription(),
                dto.getDuration(),
                dto.getGenreId()
        );
    }

    public void schedulePerformance(ScheduleDto dto) {
        jdbcTemplate.update(
                "SELECT add_performance_to_repertoire(?, ?, ?)",
                dto.getPerformanceId(),
                dto.getDate(),
                dto.getTime()
        );
    }
}
