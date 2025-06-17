package com.theatre.manager.controller;

import com.theatre.manager.dto.RepertoireDto;
import com.theatre.manager.dto.RequisiteTransaction;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Controller
@RequestMapping("/repertoire")
public class RepertoireController {

    private final JdbcTemplate jdbcTemplate;

    public RepertoireController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private boolean isAdminOrDirector(HttpSession session) {
        String role = (String) session.getAttribute("workerRole");
        return "admin".equals(role) || "director".equals(role);
    }

    @GetMapping
    public String showRepertoirePage(Model model, HttpSession session) {
        // 1) Основной репертуар
        String sql = "SELECT r.repertoire_id, r.performance_id, r.date, r.time, p.title AS performance_title " +
                "FROM repertoire r JOIN performance p ON r.performance_id = p.performance_id " +
                "ORDER BY r.date, r.time";
        List<RepertoireDto> repertoireList = jdbcTemplate.query(sql, (rs, rowNum) -> {
            RepertoireDto dto = new RepertoireDto();
            dto.setRepertoireId(rs.getLong("repertoire_id"));
            dto.setPerformanceId(rs.getLong("performance_id"));
            dto.setDate(rs.getDate("date").toLocalDate());
            dto.setTime(rs.getTime("time").toLocalTime());
            dto.setPerformanceTitle(rs.getString("performance_title"));
            return dto;
        });

        // 2) Сотрудники
        Map<Long, List<String>> workersMap = new HashMap<>();
        jdbcTemplate.query(
                "SELECT rw.repertoire_id, w.fio " +
                        "FROM repertoire_worker rw JOIN worker w ON rw.worker_id = w.worker_id",
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        workersMap
                                .computeIfAbsent(rs.getLong("repertoire_id"), k -> new ArrayList<>())
                                .add(rs.getString("fio"));
                    }
                }
        );

        // 3) Реквизит и количество
        Map<Long, List<RequisiteTransaction>> reqMap = new HashMap<>();
        jdbcTemplate.query(
                "SELECT rr.repertoire_id, r.title, rr.quantity " +
                        "FROM repertoire_requisite rr JOIN requisite r ON rr.requisite_id = r.requisite_id",
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        RequisiteTransaction rt = new RequisiteTransaction();
                        rt.setTitle(rs.getString("title"));
                        rt.setQuantity(rs.getInt("quantity"));
                        reqMap
                                .computeIfAbsent(rs.getLong("repertoire_id"), k -> new ArrayList<>())
                                .add(rt);
                    }
                }
        );

        // 4) Заполняем DTO
        for (RepertoireDto dto : repertoireList) {
            dto.setWorkerNames(workersMap.getOrDefault(dto.getRepertoireId(), Collections.emptyList()));
            dto.setRequisites(reqMap.getOrDefault(dto.getRepertoireId(), Collections.emptyList()));
        }

        model.addAttribute("repertoireList", repertoireList);
        model.addAttribute("performanceList", jdbcTemplate.queryForList("SELECT performance_id, title FROM performance"));
        model.addAttribute("workerList", jdbcTemplate.queryForList("SELECT worker_id, fio FROM worker"));
        model.addAttribute("requisiteList", jdbcTemplate.queryForList("SELECT requisite_id, title FROM requisite"));
        model.addAttribute("isAdminOrDirector", isAdminOrDirector(session));
        return "repertoire";
    }

    @PostMapping("/add")
    public String addRepertoire(@RequestParam Long performanceId,
                                @RequestParam String date,
                                @RequestParam String time,
                                @RequestParam(required = false) List<Long> workerIds,
                                @RequestParam(required = false) List<Long> requisiteIds,
                                @RequestParam(required = false) List<Integer> quantities,
                                HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/repertoire?error=accessDenied";
        }
        jdbcTemplate.update(
                "INSERT INTO repertoire (performance_id, date, time) VALUES (?, ?, ?)",
                performanceId,
                Date.valueOf(LocalDate.parse(date)),
                Time.valueOf(LocalTime.parse(time))
        );
        Long repertoireId = jdbcTemplate.queryForObject(
                "SELECT currval(pg_get_serial_sequence('repertoire','repertoire_id'))", Long.class);
        if (workerIds != null) {
            for (Long w : workerIds) {
                jdbcTemplate.update("INSERT INTO repertoire_worker (repertoire_id, worker_id) VALUES (?, ?)", repertoireId, w);
            }
        }
        if (requisiteIds != null && quantities != null && requisiteIds.size() == quantities.size()) {
            for (int i = 0; i < requisiteIds.size(); i++) {
                jdbcTemplate.update(
                        "INSERT INTO repertoire_requisite (repertoire_id, requisite_id, quantity) VALUES (?, ?, ?)",
                        repertoireId, requisiteIds.get(i), quantities.get(i)
                );
            }
        }
        return "redirect:/repertoire";
    }

    @PostMapping("/delete/{id}")
    public String deleteRepertoire(@PathVariable Long id, HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/repertoire?error=accessDenied";
        }
        jdbcTemplate.update("DELETE FROM repertoire_worker WHERE repertoire_id = ?", id);
        jdbcTemplate.update("DELETE FROM repertoire_requisite WHERE repertoire_id = ?", id);
        jdbcTemplate.update("DELETE FROM repertoire WHERE repertoire_id = ?", id);
        return "redirect:/repertoire";
    }

    @GetMapping("/edit/{id}")
    public String editRepertoireForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/repertoire?error=accessDenied";
        }
        RepertoireDto dto = jdbcTemplate.queryForObject(
                "SELECT repertoire_id, performance_id, date, time FROM repertoire WHERE repertoire_id = ?",
                new Object[]{id},
                (rs, rn) -> {
                    RepertoireDto rd = new RepertoireDto();
                    rd.setRepertoireId(rs.getLong("repertoire_id"));
                    rd.setPerformanceId(rs.getLong("performance_id"));
                    rd.setDate(rs.getDate("date").toLocalDate());
                    rd.setTime(rs.getTime("time").toLocalTime());
                    return rd;
                }
        );
        List<Long> workerIds = jdbcTemplate.queryForList(
                "SELECT worker_id FROM repertoire_worker WHERE repertoire_id = ?", Long.class, id);
        List<Long> reqIds = jdbcTemplate.queryForList(
                "SELECT requisite_id FROM repertoire_requisite WHERE repertoire_id = ?", Long.class, id);
        List<Integer> qtys = jdbcTemplate.queryForList(
                "SELECT quantity FROM repertoire_requisite WHERE repertoire_id = ?", Integer.class, id);

        model.addAttribute("repertoire", dto);
        model.addAttribute("workerIds", workerIds);
        model.addAttribute("requisiteIds", reqIds);
        model.addAttribute("quantities", qtys);
        model.addAttribute("performanceList", jdbcTemplate.queryForList("SELECT performance_id, title FROM performance"));
        model.addAttribute("workerList", jdbcTemplate.queryForList("SELECT worker_id, fio FROM worker"));
        model.addAttribute("requisiteList", jdbcTemplate.queryForList("SELECT requisite_id, title FROM requisite"));
        return "repertoire_edit";
    }

    @PostMapping("/edit/{id}")
    public String updateRepertoire(@PathVariable Long id,
                                   @RequestParam Long performanceId,
                                   @RequestParam String date,
                                   @RequestParam String time,
                                   @RequestParam(required = false) List<Long> workerIds,
                                   @RequestParam(required = false) List<Long> requisiteIds,
                                   @RequestParam(required = false) List<Integer> quantities,
                                   HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/repertoire?error=accessDenied";
        }
        jdbcTemplate.update(
                "UPDATE repertoire SET performance_id = ?, date = ?, time = ? WHERE repertoire_id = ?",
                performanceId,
                Date.valueOf(LocalDate.parse(date)),
                Time.valueOf(LocalTime.parse(time)),
                id
        );
        jdbcTemplate.update("DELETE FROM repertoire_worker WHERE repertoire_id = ?", id);
        jdbcTemplate.update("DELETE FROM repertoire_requisite WHERE repertoire_id = ?", id);
        if (workerIds != null) {
            for (Long w : workerIds) {
                jdbcTemplate.update("INSERT INTO repertoire_worker (repertoire_id, worker_id) VALUES (?, ?)", id, w);
            }
        }
        if (requisiteIds != null && quantities != null && requisiteIds.size() == quantities.size()) {
            for (int i = 0; i < requisiteIds.size(); i++) {
                jdbcTemplate.update(
                        "INSERT INTO repertoire_requisite (repertoire_id, requisite_id, quantity) VALUES (?, ?, ?)",
                        id, requisiteIds.get(i), quantities.get(i)
                );
            }
        }
        return "redirect:/repertoire";
    }
}
