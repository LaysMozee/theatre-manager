package com.theatre.manager.controller;

import com.theatre.manager.dto.ConditionDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ConditionsController {

    private final JdbcTemplate jdbc;

    public ConditionsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/conditions")
    public String list(Model model, HttpSession session) {
        String sql = """
            SELECT c.condition_id,
                   c.date,
                   ct.condition_type_name AS typeName,
                   r.title AS requisite,
                   c.quantity,
                   c.comment
            FROM conditions c
              JOIN condition_type ct
                ON c.condition_type_id = ct.condition_type_id
              JOIN requisite r
                ON c.requisite_id = r.requisite_id
            ORDER BY c.date DESC
            """;

        List<ConditionDto> list = new ArrayList<>();
        jdbc.query(sql, (RowCallbackHandler) rs -> {
            list.add(new ConditionDto(
                    rs.getLong("condition_id"),
                    rs.getDate("date").toLocalDate(),
                    rs.getString("typeName"),
                    rs.getString("requisite"),
                    rs.getInt("quantity"),
                    rs.getString("comment")
            ));
        });

        model.addAttribute("conditions", list);
        return "conditions";
    }
}
