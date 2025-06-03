package com.theatre.manager.dao;

import com.theatre.manager.entity.Requisite;
import com.theatre.manager.model.RequisiteView;
import com.theatre.manager.model.ConditionView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Repository
public class RequisiteDao {

    private final JdbcTemplate jdbcTemplate;

    public RequisiteDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ---------------------------- RequisiteView с условиями ----------------------------

    public List<RequisiteView> findAllWithConditions() {
        String sql = """
            SELECT
                r.requisite_id AS id,
                r.title AS title,
                c.date AS conditionDate,
                ct.condition_type_name AS conditionTypeTitle
            FROM requisite r
            LEFT JOIN conditions c ON c.requisite_id = r.requisite_id
            LEFT JOIN condition_type ct ON c.condition_type_id = ct.condition_type_id
            ORDER BY r.requisite_id
            """;

        return jdbcTemplate.query(sql, new RowMapper<RequisiteView>() {
            @Override
            public RequisiteView mapRow(ResultSet rs, int rowNum) throws SQLException {
                RequisiteView view = new RequisiteView();
                view.setId(rs.getLong("id"));
                view.setTitle(rs.getString("title"));
                view.setConditionDate(rs.getDate("conditionDate"));
                view.setConditionTypeTitle(rs.getString("conditionTypeTitle"));
                return view;
            }
        });
    }

    // ---------------------------- Простые реквизиты ----------------------------

    public List<Requisite> findAllRequisitesSimple() {
        String sql = """
            SELECT
                r.requisite_id,
                r.title,
                r.type,
                r.size,
                r.available_quantity,
                r.total_quantity
            FROM requisite r
            ORDER BY r.requisite_id
            """;

        return jdbcTemplate.query(sql, new RowMapper<Requisite>() {
            @Override
            public Requisite mapRow(ResultSet rs, int rowNum) throws SQLException {
                Requisite r = new Requisite();
                r.setRequisiteId(rs.getLong("requisite_id"));
                r.setTitle(rs.getString("title"));
                r.setType(rs.getString("type"));
                r.setSize(rs.getString("size"));
                r.setAvailableQuantity(rs.getInt("available_quantity"));
                r.setTotalQuantity(rs.getInt("total_quantity"));
                return r;
            }
        });
    }

    // ---------------------------- CRUD операции ----------------------------

    public void save(Requisite r) {
        String sql = """
            INSERT INTO requisite(title, type, total_quantity, available_quantity, size)
            VALUES (?, ?, ?, ?, ?)
            """;
        jdbcTemplate.update(sql,
                r.getTitle(),
                r.getType(),
                r.getTotalQuantity(),
                r.getAvailableQuantity(),
                r.getSize());
    }

    public Requisite findById(Long id) {
        String sql = """
            SELECT requisite_id, title, type, total_quantity, available_quantity, size
            FROM requisite
            WHERE requisite_id = ?
            """;
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                Requisite r = new Requisite();
                r.setRequisiteId(rs.getLong("requisite_id"));
                r.setTitle(rs.getString("title"));
                r.setType(rs.getString("type"));
                r.setTotalQuantity(rs.getInt("total_quantity"));
                r.setAvailableQuantity(rs.getInt("available_quantity"));
                r.setSize(rs.getString("size"));
                return r;
            }
            return null;
        }, id);
    }

    public void update(Requisite r) {
        String sql = """
            UPDATE requisite
            SET title = ?, type = ?, total_quantity = ?, available_quantity = ?, size = ?
            WHERE requisite_id = ?
            """;
        jdbcTemplate.update(sql,
                r.getTitle(),
                r.getType(),
                r.getTotalQuantity(),
                r.getAvailableQuantity(),
                r.getSize(),
                r.getRequisiteId());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM requisite WHERE requisite_id = ?";
        jdbcTemplate.update(sql, id);
    }

    // Методы для conditions (уже были написаны ранее)…
    public List<ConditionView> findAllConditions() { /* … */ return List.of(); }
    public void saveCondition(Long requisiteId, Long conditionTypeId, java.util.Date date) { /* … */ }
    public ConditionView findConditionById(Long conditionId) { /* … */ return null; }
    public void updateCondition(Long conditionId, Long conditionTypeId, java.util.Date date) { /* … */ }
    public void deleteConditionById(Long conditionId) { /* … */ }
}