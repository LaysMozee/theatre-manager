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
    public boolean isRequisiteUsedInRepertoire(Long requisiteId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM repertoire_requisite WHERE requisite_id = ?",
                Integer.class, requisiteId);
        return count != null && count > 0;
    }
    @Transactional
    public void deleteById(Long conditionId) {
        String sql = "DELETE FROM conditions WHERE condition_id = ?";
        jdbcTemplate.update(sql, conditionId);
    }

    public List<ConditionType> findAllConditionTypes() {
        return jdbcTemplate.query(
                "SELECT condition_type_id, condition_type_name FROM condition_type ORDER BY condition_type_id",
                (rs, rowNum) -> {
                    ConditionType type = new ConditionType();
                    type.setConditionTypeId(rs.getInt("condition_type_id"));
                    type.setName(rs.getString("condition_type_name")); // Используем правильное имя колонки
                    return type;
                });
    }

    public boolean isRequisiteUsed(Long requisiteId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM conditions WHERE requisite_id = ? AND condition_type_id = 7",
                Integer.class, requisiteId);
        return count != null && count > 0;
    }

    public List<ConditionWithTitle> findAllWithRequisiteTitles() {
        String sql = "SELECT c.condition_id, c.requisite_id, r.title as requisite_title, " +
                "c.condition_type_id, ct.name as condition_type_name, " +
                "c.quantity, c.comment, c.date " +
                "FROM conditions c " +
                "JOIN requisite r ON c.requisite_id = r.requisite_id " +
                "JOIN condition_type ct ON c.condition_type_id = ct.condition_type_id " +
                "ORDER BY c.date DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ConditionWithTitle condition = new ConditionWithTitle();
            condition.setConditionId(rs.getLong("condition_id"));
            condition.setRequisiteId(rs.getLong("requisite_id"));
            condition.setRequisiteTitle(rs.getString("requisite_title"));
            condition.setConditionTypeId(rs.getInt("condition_type_id"));
            condition.setConditionTypeName(rs.getString("condition_type_name"));
            condition.setQuantity(rs.getInt("quantity"));
            condition.setComment(rs.getString("comment"));
            condition.setDate(rs.getDate("date").toLocalDate());
            return condition;
        });
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