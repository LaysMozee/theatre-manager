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

    @GetMapping
    public String showRepertoirePage(Model model, HttpSession session) {

        List<RepertoireDto> repertoireList = jdbcTemplate.query(
                "SELECT r.repertoire_id, r.performance_id, r.date, r.time, p.title AS performance_title " +
                        "FROM repertoire r JOIN performance p ON r.performance_id = p.performance_id " +
                        "ORDER BY r.date, r.time",
                (rs, rowNum) -> {
                    RepertoireDto dto = new RepertoireDto();
                    dto.setRepertoireId(rs.getLong("repertoire_id"));
                    dto.setPerformanceId(rs.getLong("performance_id"));
                    dto.setDate(rs.getDate("date").toLocalDate());
                    dto.setTime(rs.getTime("time").toLocalTime());
                    dto.setPerformanceTitle(rs.getString("performance_title"));
                    return dto;
                });

        Map<Long, List<String>> workersMap = new HashMap<>();
        jdbcTemplate.query(
                "SELECT rw.repertoire_id, w.fio FROM repertoire_worker rw JOIN worker w ON rw.worker_id = w.worker_id",
                (RowCallbackHandler) rs -> workersMap
                        .computeIfAbsent(rs.getLong("repertoire_id"), k -> new ArrayList<>())
                        .add(rs.getString("fio")));

        Map<Long, List<RequisiteTransaction>> reqMap = new HashMap<>();
        jdbcTemplate.query(
                "SELECT rr.repertoire_id, r.title, rr.quantity, r.requisite_id, r.available_quantity " +
                        "FROM repertoire_requisite rr JOIN requisite r ON rr.requisite_id = r.requisite_id",
                (RowCallbackHandler) rs -> {
                    RequisiteTransaction rt = new RequisiteTransaction();
                    rt.setTitle(rs.getString("title"));
                    rt.setQuantity(rs.getInt("quantity"));
                    rt.setRequisiteId(rs.getLong("requisite_id"));
                    rt.setAvailableQuantity(rs.getInt("available_quantity"));
                    reqMap.computeIfAbsent(rs.getLong("repertoire_id"), k -> new ArrayList<>()).add(rt);
                });

        repertoireList.forEach(dto -> {
            dto.setWorkerNames(workersMap.getOrDefault(dto.getRepertoireId(), Collections.emptyList()));
            dto.setRequisites(reqMap.getOrDefault(dto.getRepertoireId(), Collections.emptyList()));
        });

        model.addAttribute("repertoireList", repertoireList);
        model.addAttribute("performanceList", jdbcTemplate.queryForList("SELECT performance_id, title FROM performance"));
        model.addAttribute("workerList", jdbcTemplate.queryForList("SELECT worker_id, fio FROM worker"));
        model.addAttribute("requisiteList", jdbcTemplate.queryForList(
                "SELECT requisite_id, title, available_quantity FROM requisite"));
        model.addAttribute("isAdminOrDirector", isAdminOrDirector(session));
        return "repertoire";
    }

    @GetMapping("/edit/{id}")
    public String editRepertoireForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/repertoire?error=access_denied";
        }

        RepertoireDto dto = jdbcTemplate.queryForObject(
                "SELECT r.repertoire_id, r.performance_id, r.date, r.time, p.title AS performance_title " +
                        "FROM repertoire r JOIN performance p ON r.performance_id = p.performance_id " +
                        "WHERE r.repertoire_id = ?",
                (rs, rowNum) -> {
                    RepertoireDto rdto = new RepertoireDto();
                    rdto.setRepertoireId(rs.getLong("repertoire_id"));
                    rdto.setPerformanceId(rs.getLong("performance_id"));
                    rdto.setDate(rs.getDate("date").toLocalDate());
                    rdto.setTime(rs.getTime("time").toLocalTime());
                    rdto.setPerformanceTitle(rs.getString("performance_title"));
                    return rdto;
                }, id);

        List<Long> workerIds = jdbcTemplate.queryForList(
                "SELECT worker_id FROM repertoire_worker WHERE repertoire_id = ?", Long.class, id);

        List<RequisiteTransaction> requisiteTransactions = jdbcTemplate.query(
                "SELECT rr.requisite_id, r.title, rr.quantity, r.available_quantity " +
                        "FROM repertoire_requisite rr JOIN requisite r ON rr.requisite_id = r.requisite_id " +
                        "WHERE rr.repertoire_id = ?",
                (rs, rowNum) -> {
                    RequisiteTransaction rt = new RequisiteTransaction();
                    rt.setRequisiteId(rs.getLong("requisite_id"));
                    rt.setTitle(rs.getString("title"));
                    rt.setQuantity(rs.getInt("quantity"));
                    rt.setAvailableQuantity(rs.getInt("available_quantity"));
                    return rt;
                }, id);

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
                                   HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/repertoire?error=access_denied";
        }

        try {
            // вывод старого рек
            List<Map<String, Object>> oldRequisites = jdbcTemplate.queryForList(
                    "SELECT requisite_id, quantity FROM repertoire_requisite WHERE repertoire_id = ?", id);

            for (Map<String, Object> req : oldRequisites) {
                jdbcTemplate.update(
                        "UPDATE requisite SET available_quantity = available_quantity + ? WHERE requisite_id = ?",
                        req.get("quantity"), req.get("requisite_id"));
            }

            //
            jdbcTemplate.update(
                    "UPDATE repertoire SET performance_id = ?, date = ?, time = ? WHERE repertoire_id = ?",
                    performanceId, Date.valueOf(LocalDate.parse(date)), Time.valueOf(LocalTime.parse(time)), id);

            // удалить старый сотрудников и добавить новых
            jdbcTemplate.update("DELETE FROM repertoire_worker WHERE repertoire_id = ?", id);
            if (workerIds != null) {
                for (Long workerId : workerIds) {
                    jdbcTemplate.update(
                            "INSERT INTO repertoire_worker (repertoire_id, worker_id) VALUES (?, ?)",
                            id, workerId);
                }
            }

            // удалить старый реквез и добавить новых
            jdbcTemplate.update("DELETE FROM repertoire_requisite WHERE repertoire_id = ?", id);
            if (requisiteIds != null && quantities != null) {
                for (int i = 0; i < requisiteIds.size(); i++) {
                    jdbcTemplate.update(
                            "INSERT INTO repertoire_requisite (repertoire_id, requisite_id, quantity) VALUES (?, ?, ?)",
                            id, requisiteIds.get(i), quantities.get(i));

                    jdbcTemplate.update(
                            "UPDATE requisite SET available_quantity = available_quantity - ? WHERE requisite_id = ?",
                            quantities.get(i), requisiteIds.get(i));

                    jdbcTemplate.update(
                            "INSERT INTO conditions (requisite_id, date, quantity, comment, condition_type_id) " +
                                    "VALUES (?, ?, ?, ?, 7)",
                            requisiteIds.get(i), LocalDate.now(), quantities.get(i),
                            "Задействован в репертуаре ID " + id);
                }
            }

            return "redirect:/repertoire?success=true";
        } catch (Exception e) {
            return "redirect:/repertoire?error=update_failed";
        }
    }

    @PostMapping("/add")
    @Transactional
    public String addRepertoire(@RequestParam Long performanceId,
                                @RequestParam String date,
                                @RequestParam String time,
                                @RequestParam(required = false) List<Long> workerIds,
                                @RequestParam(required = false) List<Long> requisiteIds,
                                @RequestParam(required = false) List<Integer> quantities,
                                HttpSession session, Model model) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/repertoire?error=access_denied";
        }

        try {
            jdbcTemplate.update(
                    "INSERT INTO repertoire (performance_id, date, time) VALUES (?, ?, ?)",
                    performanceId, Date.valueOf(LocalDate.parse(date)), Time.valueOf(LocalTime.parse(time)));

            Long repertoireId = jdbcTemplate.queryForObject(
                    "SELECT currval(pg_get_serial_sequence('repertoire','repertoire_id'))", Long.class);

            if (workerIds != null) {
                workerIds.forEach(workerId -> jdbcTemplate.update(
                        "INSERT INTO repertoire_worker (repertoire_id, worker_id) VALUES (?, ?)",
                        repertoireId, workerId));
            }

            if (requisiteIds != null && quantities != null) {
                for (int i = 0; i < requisiteIds.size(); i++) {
                    jdbcTemplate.update(
                            "INSERT INTO repertoire_requisite (repertoire_id, requisite_id, quantity) VALUES (?, ?, ?)",
                            repertoireId, requisiteIds.get(i), quantities.get(i));

                    jdbcTemplate.update(
                            "UPDATE requisite SET available_quantity = available_quantity - ? WHERE requisite_id = ?",
                            quantities.get(i), requisiteIds.get(i));

                    jdbcTemplate.update(
                            "INSERT INTO conditions (requisite_id, date, quantity, comment, condition_type_id) " +
                                    "VALUES (?, ?, ?, ?, 7)",
                            requisiteIds.get(i), LocalDate.now(), quantities.get(i),
                            "Задействован в репертуаре ID " + repertoireId);
                }
            }

            return "redirect:/repertoire?success=true";
        } catch (Exception e) {
            return "redirect:/repertoire?error=save_failed";
        }
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteRepertoire(@PathVariable Long id, HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/repertoire?error=access_denied";
        }

        try {
            jdbcTemplate.queryForList(
                            "SELECT requisite_id, quantity FROM repertoire_requisite WHERE repertoire_id = ?", id)
                    .forEach(req -> jdbcTemplate.update(
                            "UPDATE requisite SET available_quantity = available_quantity + ? WHERE requisite_id = ?",
                            req.get("quantity"), req.get("requisite_id")));

            jdbcTemplate.update("DELETE FROM repertoire_worker WHERE repertoire_id = ?", id);
            jdbcTemplate.update("DELETE FROM repertoire_requisite WHERE repertoire_id = ?", id);
            jdbcTemplate.update("DELETE FROM repertoire WHERE repertoire_id = ?", id);

            return "redirect:/repertoire?success=true";
        } catch (Exception e) {
            return "redirect:/repertoire?error=delete_failed";
        }
    }

    private boolean isAdminOrDirector(HttpSession session) {
        String role = (String) session.getAttribute("workerRole");
        return "admin".equals(role) || "director".equals(role);
    }
}