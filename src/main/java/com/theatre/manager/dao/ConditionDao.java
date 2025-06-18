package com.theatre.manager.dao;

import com.theatre.manager.model.ConditionWithTitle;
import com.theatre.manager.model.Condition;
import com.theatre.manager.model.ConditionType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public class ConditionDao {

    private final JdbcTemplate jdbcTemplate;

    public ConditionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void addCondition(Condition condition) {
        String sql = "INSERT INTO conditions (requisite_id, date, quantity, comment, condition_type_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                condition.getRequisiteId(),
                condition.getDate(),
                condition.getQuantity(),
                condition.getComment(),
                condition.getConditionTypeId());

        if (condition.getConditionTypeId() == 7) {
            updateRequisiteQuantity(condition.getRequisiteId(), -condition.getQuantity());
        }
    }

    public List<Condition> findAllConditions() {
        String sql = "SELECT c.*, r.title as requisite_title, ct.name as condition_type_name " +
                "FROM conditions c " +
                "JOIN requisite r ON c.requisite_id = r.requisite_id " +
                "JOIN condition_type ct ON c.condition_type_id = ct.condition_type_id " +
                "ORDER BY c.date DESC";

        return jdbcTemplate.query(sql, new ConditionRowMapper());
    }

    public Condition findById(Long conditionId) {
        String sql = "SELECT c.*, r.title as requisite_title, ct.name as condition_type_name " +
                "FROM conditions c " +
                "JOIN requisite r ON c.requisite_id = r.requisite_id " +
                "JOIN condition_type ct ON c.condition_type_id = ct.condition_type_id " +
                "WHERE c.condition_id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new ConditionRowMapper(), conditionId);
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void updateCondition(Condition condition) {
        Condition oldCondition = findById(condition.getConditionId());

        if (oldCondition != null && oldCondition.getConditionTypeId() == 7) {
            updateRequisiteQuantity(oldCondition.getRequisiteId(), oldCondition.getQuantity());
        }

        String sql = "UPDATE conditions SET requisite_id = ?, date = ?, quantity = ?, " +
                "comment = ?, condition_type_id = ? WHERE condition_id = ?";

        jdbcTemplate.update(sql,
                condition.getRequisiteId(),
                condition.getDate(),
                condition.getQuantity(),
                condition.getComment(),
                condition.getConditionTypeId(),
                condition.getConditionId());

        if (condition.getConditionTypeId() == 7) {
            updateRequisiteQuantity(condition.getRequisiteId(), -condition.getQuantity());
        }
    }

    @Transactional
    public void deleteById(Long conditionId) {
        Condition condition = findById(conditionId);

        if (condition != null) {
            if (condition.getConditionTypeId() == 7) {
                updateRequisiteQuantity(condition.getRequisiteId(), condition.getQuantity());
            }

            String sql = "DELETE FROM conditions WHERE condition_id = ?";
            jdbcTemplate.update(sql, conditionId);
        }
    }

    public List<ConditionType> findAllConditionTypes() {
        String sql = "SELECT * FROM condition_type ORDER BY condition_type_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ConditionType type = new ConditionType();
            type.setConditionTypeId(rs.getInt("condition_type_id"));
            type.setName(rs.getString("name"));
            type.setDescription(rs.getString("description"));
            return type;
        });
    }

    public List<ConditionWithTitle> findAllWithRequisiteTitles() {
        String sql = "SELECT c.*, r.title as requisite_title, ct.name as condition_type_name " +
                "FROM conditions c " +
                "JOIN requisite r ON c.requisite_id = r.requisite_id " +
                "JOIN condition_type ct ON c.condition_type_id = ct.condition_type_id " +
                "ORDER BY c.date DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ConditionWithTitle condition = new ConditionWithTitle();
            condition.setConditionId(rs.getLong("condition_id"));
            condition.setRequisiteId(rs.getLong("requisite_id"));
            condition.setDate(rs.getObject("date", LocalDate.class));
            condition.setQuantity(rs.getInt("quantity"));
            condition.setComment(rs.getString("comment"));
            condition.setConditionTypeId(rs.getInt("condition_type_id"));
            condition.setRequisiteTitle(rs.getString("requisite_title"));
            condition.setConditionTypeName(rs.getString("condition_type_name"));
            return condition;
        });
    }

    private void updateRequisiteQuantity(Long requisiteId, int delta) {
        String sql = "UPDATE requisite SET available_quantity = available_quantity + ? WHERE requisite_id = ?";
        jdbcTemplate.update(sql, delta, requisiteId);
    }

    private static class ConditionRowMapper implements RowMapper<Condition> {
        @Override
        public Condition mapRow(ResultSet rs, int rowNum) throws SQLException {
            Condition condition = new Condition();
            condition.setConditionId(rs.getLong("condition_id"));
            condition.setRequisiteId(rs.getLong("requisite_id"));
            condition.setDate(rs.getObject("date", LocalDate.class));
            condition.setQuantity(rs.getInt("quantity"));
            condition.setComment(rs.getString("comment"));
            condition.setConditionTypeId(rs.getInt("condition_type_id"));
            condition.setRequisiteTitle(rs.getString("requisite_title"));
            condition.setConditionTypeName(rs.getString("condition_type_name"));
            return condition;
        }
    }
}