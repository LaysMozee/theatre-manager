package com.theatre.manager.controller;

import com.theatre.manager.dto.RepertoireDto;
import com.theatre.manager.dto.RequisiteTransaction;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
        // 1) Main repertoire
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

        // 2) Workers
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

        // 3) Requisites and quantities
        Map<Long, List<RequisiteTransaction>> reqMap = new HashMap<>();
        jdbcTemplate.query(
                "SELECT rr.repertoire_id, r.title, rr.quantity, r.requisite_id, r.available_quantity " +
                        "FROM repertoire_requisite rr JOIN requisite r ON rr.requisite_id = r.requisite_id",
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        RequisiteTransaction rt = new RequisiteTransaction();
                        rt.setTitle(rs.getString("title"));
                        rt.setQuantity(rs.getInt("quantity"));
                        rt.setRequisiteId(rs.getLong("requisite_id"));
                        rt.setAvailableQuantity(rs.getInt("available_quantity"));
                        reqMap
                                .computeIfAbsent(rs.getLong("repertoire_id"), k -> new ArrayList<>())
                                .add(rt);
                    }
                }
        );

        // 4) Fill DTO
        for (RepertoireDto dto : repertoireList) {
            dto.setWorkerNames(workersMap.getOrDefault(dto.getRepertoireId(), Collections.emptyList()));
            dto.setRequisites(reqMap.getOrDefault(dto.getRepertoireId(), Collections.emptyList()));
        }

        model.addAttribute("repertoireList", repertoireList);
        model.addAttribute("performanceList", jdbcTemplate.queryForList("SELECT performance_id, title FROM performance"));
        model.addAttribute("workerList", jdbcTemplate.queryForList("SELECT worker_id, fio FROM worker"));
        model.addAttribute("requisiteList", jdbcTemplate.queryForList(
                "SELECT requisite_id as requisite_id, title as title, available_quantity as available_quantity FROM requisite"));
        model.addAttribute("isAdminOrDirector", isAdminOrDirector(session));
        return "repertoire";
    }

    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)  // Add this
    public String addRepertoire(@RequestParam Long performanceId,
                                @RequestParam String date,
                                @RequestParam String time,
                                @RequestParam(required = false) List<Long> workerIds,
                                @RequestParam(required = false) List<Long> requisiteIds,
                                @RequestParam(required = false) List<Integer> quantities,
                                HttpSession session, Model model) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/repertoire?error=accessDenied";
        }

        // Validate input
        if (requisiteIds != null && quantities != null && requisiteIds.size() != quantities.size()) {
            model.addAttribute("error", "Mismatch between requisites and quantities");
            return showRepertoirePage(model, session);
        }

        // Check available quantities
        if (requisiteIds != null && quantities != null) {
            for (int i = 0; i < requisiteIds.size(); i++) {
                Integer available = jdbcTemplate.queryForObject(
                        "SELECT available_quantity FROM requisite WHERE requisite_id = ?",
                        Integer.class, requisiteIds.get(i));
                if (available == null || available < quantities.get(i)) {
                    model.addAttribute("error", "Not enough requisites with ID " + requisiteIds.get(i) +
                            ". Available: " + available + ", required: " + quantities.get(i));
                    return showRepertoirePage(model, session);
                }
            }
        }

        try {
            // Insert repertoire
            jdbcTemplate.update(
                    "INSERT INTO repertoire (performance_id, date, time) VALUES (?, ?, ?)",
                    performanceId,
                    Date.valueOf(LocalDate.parse(date)),
                    Time.valueOf(LocalTime.parse(time)));

            // Get the new repertoire ID
            Long repertoireId = jdbcTemplate.queryForObject(
                    "SELECT currval(pg_get_serial_sequence('repertoire','repertoire_id'))", Long.class);

            // Add workers
            if (workerIds != null) {
                for (Long workerId : workerIds) {
                    jdbcTemplate.update(
                            "INSERT INTO repertoire_worker (repertoire_id, worker_id) VALUES (?, ?)",
                            repertoireId, workerId);
                }
            }

            // Add requisites and update conditions
            if (requisiteIds != null && quantities != null) {
                for (int i = 0; i < requisiteIds.size(); i++) {
                    // Add to repertoire_requisite
                    jdbcTemplate.update(
                            "INSERT INTO repertoire_requisite (repertoire_id, requisite_id, quantity) VALUES (?, ?, ?)",
                            repertoireId, requisiteIds.get(i), quantities.get(i));

                    // Update available quantity
                    jdbcTemplate.update(
                            "UPDATE requisite SET available_quantity = available_quantity - ? WHERE requisite_id = ?",
                            quantities.get(i), requisiteIds.get(i));

                    // Add to conditions (type 7 - "Used in repertoire")
                    jdbcTemplate.update(
                            "INSERT INTO conditions (requisite_id, date, quantity, comment, condition_type_id) " +
                                    "VALUES (?, ?, ?, ?, 7)",
                            requisiteIds.get(i),
                            LocalDate.now(),
                            quantities.get(i),
                            "Задействован в репертуаре ID " + repertoireId);
                }
            }

            return "redirect:/repertoire?success=true";
        } catch (Exception e) {
            model.addAttribute("error", "Error while adding: " + e.getMessage());
            return showRepertoirePage(model, session);
        }
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteRepertoire(@PathVariable Long id, HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/repertoire?error=accessDenied";
        }

        try {
            // First return all requisites quantities
            List<Map<String, Object>> requisites = jdbcTemplate.queryForList(
                    "SELECT requisite_id, quantity FROM repertoire_requisite WHERE repertoire_id = ?", id);

            for (Map<String, Object> req : requisites) {
                jdbcTemplate.update(
                        "UPDATE requisite SET available_quantity = available_quantity + ? WHERE requisite_id = ?",
                        req.get("quantity"), req.get("requisite_id"));
            }

            // Then delete all related records
            jdbcTemplate.update("DELETE FROM repertoire_worker WHERE repertoire_id = ?", id);
            jdbcTemplate.update("DELETE FROM repertoire_requisite WHERE repertoire_id = ?", id);
            jdbcTemplate.update("DELETE FROM repertoire WHERE repertoire_id = ?", id);

            return "redirect:/repertoire";
        } catch (Exception e) {
            return "redirect:/repertoire?error=deleteFailed";
        }
    }

    @GetMapping("/edit/{id}")
    public String editRepertoireForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/repertoire?error=accessDenied";
        }

        // Get repertoire data
        RepertoireDto dto = jdbcTemplate.queryForObject(
                "SELECT r.repertoire_id, r.performance_id, r.date, r.time, p.title AS performance_title " +
                        "FROM repertoire r JOIN performance p ON r.performance_id = p.performance_id " +
                        "WHERE r.repertoire_id = ?",
                new Object[]{id},
                (rs, rn) -> {
                    RepertoireDto rd = new RepertoireDto();
                    rd.setRepertoireId(rs.getLong("repertoire_id"));
                    rd.setPerformanceId(rs.getLong("performance_id"));
                    rd.setDate(rs.getDate("date").toLocalDate());
                    rd.setTime(rs.getTime("time").toLocalTime());
                    rd.setPerformanceTitle(rs.getString("performance_title"));
                    return rd;
                }
        );

        // Get workers list
        List<Long> workerIds = jdbcTemplate.queryForList(
                "SELECT worker_id FROM repertoire_worker WHERE repertoire_id = ?", Long.class, id);

        // Get requisites list
        List<RequisiteTransaction> requisiteTransactions = jdbcTemplate.query(
                "SELECT rr.requisite_id, r.title, rr.quantity, r.available_quantity " +
                        "FROM repertoire_requisite rr JOIN requisite r ON rr.requisite_id = r.requisite_id " +
                        "WHERE rr.repertoire_id = ?",
                new Object[]{id},
                (rs, rn) -> {
                    RequisiteTransaction rt = new RequisiteTransaction();
                    rt.setRequisiteId(rs.getLong("requisite_id"));
                    rt.setTitle(rs.getString("title"));
                    rt.setQuantity(rs.getInt("quantity"));
                    rt.setAvailableQuantity(rs.getInt("available_quantity"));
                    return rt;
                }
        );

        model.addAttribute("editingRepertoire", dto);
        model.addAttribute("workerIds", workerIds);
        model.addAttribute("requisiteTransactions", requisiteTransactions);
        model.addAttribute("performanceList", jdbcTemplate.queryForList("SELECT performance_id, title FROM performance"));
        model.addAttribute("workerList", jdbcTemplate.queryForList("SELECT worker_id, fio FROM worker"));
        model.addAttribute("requisiteList", jdbcTemplate.queryForList(
                "SELECT requisite_id, title, available_quantity FROM requisite"));
        model.addAttribute("isAdminOrDirector", isAdminOrDirector(session));

        return "repertoire";
    }

    @PostMapping("/edit/{id}")
    @Transactional
    public String updateRepertoire(@PathVariable Long id,
                                   @RequestParam Long performanceId,
                                   @RequestParam String date,
                                   @RequestParam String time,
                                   @RequestParam(required = false) List<Long> workerIds,
                                   @RequestParam(required = false) List<Long> requisiteIds,
                                   @RequestParam(required = false) List<Integer> quantities,
                                   HttpSession session, Model model) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/repertoire?error=accessDenied";
        }

        // Check available quantities
        if (requisiteIds != null && quantities != null) {
            for (int i = 0; i < requisiteIds.size(); i++) {
                Integer available = jdbcTemplate.queryForObject(
                        "SELECT available_quantity FROM requisite WHERE requisite_id = ?",
                        Integer.class, requisiteIds.get(i));
                if (available == null || available < quantities.get(i)) {
                    model.addAttribute("error", "Not enough requisites with ID " + requisiteIds.get(i) +
                            ". Available: " + available + ", required: " + quantities.get(i));
                    return showRepertoirePage(model, session);
                }
            }
        }

        try {
            // First get old requisites values
            List<Map<String, Object>> oldRequisites = jdbcTemplate.queryForList(
                    "SELECT requisite_id, quantity FROM repertoire_requisite WHERE repertoire_id = ?", id);

            // Update main info
            jdbcTemplate.update(
                    "UPDATE repertoire SET performance_id = ?, date = ?, time = ? WHERE repertoire_id = ?",
                    performanceId,
                    Date.valueOf(LocalDate.parse(date)),
                    Time.valueOf(LocalTime.parse(time)),
                    id
            );
            jdbcTemplate.update("DELETE FROM repertoire_worker WHERE repertoire_id = ?", id);

            // Delete old records and return quantity
            for (Map<String, Object> oldReq : oldRequisites) {
                Long reqId = (Long) oldReq.get("requisite_id");
                Integer qty = (Integer) oldReq.get("quantity");
                jdbcTemplate.update(
                        "UPDATE requisite SET available_quantity = available_quantity + ? WHERE requisite_id = ?",
                        qty, reqId);
            }

            jdbcTemplate.update("DELETE FROM repertoire_requisite WHERE repertoire_id = ?", id);

            // Add new records and subtract quantity
            if (requisiteIds != null && quantities != null) {
                for (int i = 0; i < requisiteIds.size(); i++) {
                    jdbcTemplate.update(
                            "INSERT INTO repertoire_requisite (repertoire_id, requisite_id, quantity) VALUES (?, ?, ?)",
                            id, requisiteIds.get(i), quantities.get(i));

                    jdbcTemplate.update(
                            "UPDATE requisite SET available_quantity = available_quantity - ? WHERE requisite_id = ?",
                            quantities.get(i), requisiteIds.get(i));

                    // Add record to conditions
                    jdbcTemplate.update(
                            "INSERT INTO conditions (requisite_id, date, quantity, comment, condition_type_id) " +
                                    "VALUES (?, ?, ?, ?, 7)",
                            requisiteIds.get(i),
                            LocalDate.now(),
                            quantities.get(i),
                            "Задействован в репертуаре ID " + id);
                }
            }

            // Add workers
            if (workerIds != null) {
                for (Long workerId : workerIds) {
                    jdbcTemplate.update(
                            "INSERT INTO repertoire_worker (repertoire_id, worker_id) VALUES (?, ?)",
                            id, workerId);
                }
            }

            return "redirect:/repertoire";
        } catch (Exception e) {
            model.addAttribute("error", "Error while updating: " + e.getMessage());
            return showRepertoirePage(model, session);
        }
    }
}