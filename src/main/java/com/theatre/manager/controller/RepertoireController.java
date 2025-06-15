package com.theatre.manager.controller;

import com.theatre.manager.dto.RepertoireDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
        // Получаем репертуар
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
                }
        );
        model.addAttribute("repertoireList", repertoireList);

        // Получаем список всех спектаклей
        var performanceList = jdbcTemplate.queryForList("SELECT performance_id, title FROM performance");
        model.addAttribute("performanceList", performanceList);

        // Получаем всех работников
        var workerList = jdbcTemplate.queryForList("SELECT worker_id, fio FROM worker");
        model.addAttribute("workerList", workerList);

        // Получаем реквизиты
        var requisiteList = jdbcTemplate.queryForList("SELECT requisite_id, title FROM requisite");
        model.addAttribute("requisiteList", requisiteList);

        model.addAttribute("isAdminOrDirector", isAdminOrDirector(session));
        return "repertoire"; // thymeleaf шаблон
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

        // Вставляем новый репертуар
        jdbcTemplate.update(
                "INSERT INTO repertoire (performance_id, date, time) VALUES (?, ?, ?)",
                performanceId,
                Date.valueOf(LocalDate.parse(date)),
                Time.valueOf(LocalTime.parse(time))
        );

        // Получаем последний id
        Long repertoireId = jdbcTemplate.queryForObject("SELECT currval(pg_get_serial_sequence('repertoire','repertoire_id'))", Long.class);

        // Добавляем работников
        if (workerIds != null) {
            for (Long workerId : workerIds) {
                jdbcTemplate.update("INSERT INTO repertoire_worker (repertoire_id, worker_id) VALUES (?, ?)", repertoireId, workerId);
            }
        }

        // Добавляем реквизиты с количеством
        if (requisiteIds != null && quantities != null && requisiteIds.size() == quantities.size()) {
            for (int i = 0; i < requisiteIds.size(); i++) {
                jdbcTemplate.update("INSERT INTO repertoire_requisite (repertoire_id, requisite_id, quantity) VALUES (?, ?, ?)",
                        repertoireId, requisiteIds.get(i), quantities.get(i));
            }
        }

        return "redirect:/repertoire";
    }

    @GetMapping("/edit/{id}")
    public String editRepertoireForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/repertoire?error=accessDenied";
        }

        RepertoireDto repertoire = jdbcTemplate.queryForObject(
                "SELECT repertoire_id, performance_id, date, time FROM repertoire WHERE repertoire_id = ?",
                new Object[]{id},
                (rs, rowNum) -> {
                    RepertoireDto dto = new RepertoireDto();
                    dto.setRepertoireId(rs.getLong("repertoire_id"));
                    dto.setPerformanceId(rs.getLong("performance_id"));
                    dto.setDate(rs.getDate("date").toLocalDate());
                    dto.setTime(rs.getTime("time").toLocalTime());
                    return dto;
                });

        // Получаем список работников и реквизитов, связанных с этим репертуаром
        List<Long> workerIds = jdbcTemplate.queryForList(
                "SELECT worker_id FROM repertoire_worker WHERE repertoire_id = ?", Long.class, id);
        List<Long> requisiteIds = jdbcTemplate.queryForList(
                "SELECT requisite_id FROM repertoire_requisite WHERE repertoire_id = ?", Long.class, id);
        List<Integer> quantities = jdbcTemplate.queryForList(
                "SELECT quantity FROM repertoire_requisite WHERE repertoire_id = ?", Integer.class, id);

        model.addAttribute("repertoire", repertoire);
        model.addAttribute("workerIds", workerIds);
        model.addAttribute("requisiteIds", requisiteIds);
        model.addAttribute("quantities", quantities);

        model.addAttribute("performanceList", jdbcTemplate.queryForList("SELECT performance_id, title FROM performance"));
        model.addAttribute("workerList", jdbcTemplate.queryForList("SELECT worker_id, fio FROM worker"));
        model.addAttribute("requisiteList", jdbcTemplate.queryForList("SELECT requisite_id, title  FROM requisite"));

        return "repertoire_edit"; // твой шаблон формы редактирования
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

        // Обновляем данные репертуара
        jdbcTemplate.update(
                "UPDATE repertoire SET performance_id = ?, date = ?, time = ? WHERE repertoire_id = ?",
                performanceId,
                Date.valueOf(LocalDate.parse(date)),
                Time.valueOf(LocalTime.parse(time)),
                id);

        // Чистим связи с работниками и реквизитом
        jdbcTemplate.update("DELETE FROM repertoire_worker WHERE repertoire_id = ?", id);
        jdbcTemplate.update("DELETE FROM repertoire_requisite WHERE repertoire_id = ?", id);

        // Вставляем новые связи с работниками
        if (workerIds != null) {
            for (Long workerId : workerIds) {
                jdbcTemplate.update("INSERT INTO repertoire_worker (repertoire_id, worker_id) VALUES (?, ?)", id, workerId);
            }
        }

        // Вставляем новые связи с реквизитом и их количествами
        if (requisiteIds != null && quantities != null && requisiteIds.size() == quantities.size()) {
            for (int i = 0; i < requisiteIds.size(); i++) {
                jdbcTemplate.update("INSERT INTO repertoire_requisite (repertoire_id, requisite_id, quantity) VALUES (?, ?, ?)",
                        id, requisiteIds.get(i), quantities.get(i));
            }
        }

        return "redirect:/repertoire";
    }

    @PostMapping("/delete/{id}")
    public String deleteRepertoire(@PathVariable Long id, HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/repertoire?error=accessDenied";
        }

        // 1. Удаляем связи из performance_repertoire
        jdbcTemplate.update("DELETE FROM performance_repertoire WHERE repertoire_id = ?", id);

        // 2. Удаляем связи с работниками
        jdbcTemplate.update("DELETE FROM repertoire_worker WHERE repertoire_id = ?", id);

        // 3. Удаляем связи с реквизитом
        jdbcTemplate.update("DELETE FROM repertoire_requisite WHERE repertoire_id = ?", id);

        // 4. Только теперь удаляем сам репертуар
        jdbcTemplate.update("DELETE FROM repertoire WHERE repertoire_id = ?", id);

        return "redirect:/repertoire";
    }


}
