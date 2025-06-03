package com.theatre.manager.dao;

import com.theatre.manager.entity.ConditionType;
import com.theatre.manager.model.Condition;
import com.theatre.manager.model.ConditionWithTitle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

@Repository
public class ConditionDao {
    private final JdbcTemplate jdbc;

    public ConditionDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void addConditionAndUpdateRequisite(Condition condition) {
        jdbc.update("""
            INSERT INTO conditions
                (requisite_id, condition_type_id, date, comment, quantity)
            VALUES (?, ?, ?, ?, ?)
            """,
                condition.getRequisiteId(),
                condition.getConditionTypeId(),
                Date.valueOf(condition.getDate()),
                condition.getComment(),
                condition.getQuantity()
        );

        int updated = jdbc.update("""
            UPDATE requisite
            SET available_quantity = available_quantity - ?
            WHERE requisite_id = ? AND available_quantity >= ?
            """,
                condition.getQuantity(),
                condition.getRequisiteId(),
                condition.getQuantity()
        );

        if (updated == 0) {
            throw new RuntimeException("Недостаточно доступных единиц реквизита.");
        }
    }

    public List<ConditionType> findAllConditionTypes() {
        return jdbc.query(
                "SELECT condition_type_id, condition_type_name FROM condition_type",
                conditionTypeMapper
        );
    }

    public List<ConditionWithTitle> findAllWithRequisiteTitles() {
        return jdbc.query("""
            SELECT
                c.condition_id,
                c.requisite_id,
                r.title,
                ct.condition_type_name AS condition_type_title,
                c.quantity,
                c.date,
                c.comment
            FROM conditions c
            JOIN requisite r ON c.requisite_id = r.requisite_id
            JOIN condition_type ct ON c.condition_type_id = ct.condition_type_id
            ORDER BY c.date DESC
            """,
                conditionWithTitleMapper
        );
    }

    private final RowMapper<ConditionType> conditionTypeMapper = (rs, rowNum) -> {
        ConditionType ct = new ConditionType();
        ct.setConditionTypeId(rs.getInt("condition_type_id"));
        ct.setConditionTypeName(rs.getString("condition_type_name"));
        return ct;
    };

    private final RowMapper<ConditionWithTitle> conditionWithTitleMapper = (rs, rowNum) -> {
        ConditionWithTitle cwt = new ConditionWithTitle();
        cwt.setConditionId(rs.getLong("condition_id"));
        cwt.setRequisiteId(rs.getLong("requisite_id"));
        cwt.setTitle(rs.getString("title"));
        cwt.setConditionTypeTitle(rs.getString("condition_type_title"));
        cwt.setQuantity(rs.getInt("quantity"));
        cwt.setDate(rs.getDate("date"));
        cwt.setComment(rs.getString("comment"));
        return cwt;
    };

    public Condition findById(Long id) {
        return jdbc.queryForObject("""
            SELECT
                condition_id,
                requisite_id,
                condition_type_id,
                date,
                comment,
                quantity
            FROM conditions
            WHERE condition_id = ?
            """,
                (rs, rowNum) -> {
                    Condition c = new Condition();
                    c.setConditionId(rs.getLong("condition_id"));
                    c.setRequisiteId(rs.getLong("requisite_id"));
                    c.setConditionTypeId(rs.getInt("condition_type_id"));
                    c.setDate(rs.getDate("date").toLocalDate());
                    c.setComment(rs.getString("comment"));
                    c.setQuantity(rs.getInt("quantity"));
                    return c;
                },
                id
        );
    }

    @Transactional
    public void updateCondition(Condition condition) {
        jdbc.update("""
            UPDATE conditions
            SET condition_type_id = ?,
                date = ?,
                comment = ?,
                quantity = ?
            WHERE condition_id = ?
            """,
                condition.getConditionTypeId(),
                Date.valueOf(condition.getDate()),
                condition.getComment(),
                condition.getQuantity(),
                condition.getConditionId()
        );
    }

    public void deleteById(Long conditionId) {
        jdbc.update("DELETE FROM conditions WHERE condition_id = ?", conditionId);
    }

}
