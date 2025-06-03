package com.theatre.manager.mapper;

import com.theatre.manager.dto.RehearsalDto;
import com.theatre.manager.dto.WorkerDto;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RehearsalResultSetExtractor implements ResultSetExtractor<List<RehearsalDto>> {

    @Override
    public List<RehearsalDto> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, RehearsalDto> map = new LinkedHashMap<>();

        while (rs.next()) {
            Long rId = rs.getLong("rehearsal_id");
            RehearsalDto dto = map.get(rId);
            if (dto == null) {
                dto = new RehearsalDto();
                dto.setRehearsalId(rId);
                dto.setDate(rs.getDate("date").toLocalDate());
                dto.setTime(rs.getTime("time").toLocalTime());
                dto.setRehearsalRoom(rs.getString("rehearsal_room"));
                dto.setComment(rs.getString("comment"));
                map.put(rId, dto);
            }
            Long wId = rs.getLong("worker_id");
            String fio = rs.getString("fio");
            if (wId != null && fio != null) {
                dto.addWorker(new WorkerDto(wId, fio));
            }
        }
        return new ArrayList<>(map.values());
    }
}
