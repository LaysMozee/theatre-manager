package com.theatre.manager.controller;

import com.theatre.manager.dto.RepertoireDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Controller
@RequestMapping("/repertoire")
public class RepertoireController {

    private final JdbcTemplate jdbc;

    public RepertoireController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("workerRole");
        return "admin".equals(role) || "director".equals(role);
    }

    @GetMapping
    public String repertoirePage(Model model, HttpSession session) {
        String sql = """
            SELECT r.repertoire_id, r.date, r.time,
                   p.performance_id, p.title,
                   ARRAY(SELECT w.fio
                         FROM worker w
                         JOIN repertoire_worker rw ON rw.worker_id = w.worker_id
                         WHERE rw.repertoire_id = r.repertoire_id) as workers,
                   ARRAY(
                       SELECT CONCAT(req.title, ' (', rr.quantity, ')')
                       FROM repertoire_requisite rr
                       JOIN requisite req ON req.requisite_id = rr.requisite_id
                       WHERE rr.repertoire_id = r.repertoire_id
                   ) as requisites
            FROM repertoire r
            JOIN performance p ON p.performance_id = r.performance_id
            ORDER BY r.date, r.time
        """;

        List<Map<String, Object>> list = jdbc.queryForList(sql);
        model.addAttribute("repertoireList", list);
        model.addAttribute("isAdmin", isAdmin(session));
        return "repertoire";
    }

    @GetMapping({"/edit", "/edit/{id}"})
    public String editForm(@PathVariable(required = false) Long id,
                           Model model,
                           HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/repertoire?error=accessDenied";
        }

        RepertoireDto dto = new RepertoireDto();
        List<Long> workerIds = new ArrayList<>();
        List<Long> reqIds = new ArrayList<>();
        List<Integer> qtys = new ArrayList<>();

        if (id != null) {
            String sql = "SELECT repertoire_id, performance_id, date, time FROM repertoire WHERE repertoire_id = ?";
            dto = jdbc.queryForObject(sql, new Object[]{id}, (rs, rn) -> {
                var d = new RepertoireDto();
                d.setRepertoireId(rs.getLong("repertoire_id"));
                d.setPerformanceId(rs.getLong("performance_id"));
                d.setDate(rs.getDate("date").toLocalDate());
                d.setTime(rs.getTime("time").toLocalTime());
                return d;
            });

            workerIds = jdbc.queryForList("SELECT worker_id FROM repertoire_worker WHERE repertoire_id = ?", Long.class, id);
            var map = jdbc.queryForList("SELECT requisite_id, quantity FROM repertoire_requisite WHERE repertoire_id = ?", Map.class, id);
            for (Map<String, Object> row : map) {
                reqIds.add(((Number) row.get("requisite_id")).longValue());
                qtys.add(((Number) row.get("quantity")).intValue());
            }
        }

        model.addAttribute("repertoire", dto);
        model.addAttribute("workerIds", workerIds);
        model.addAttribute("requisiteIds", reqIds);
        model.addAttribute("quantities", qtys);
        model.addAttribute("performanceList", jdbc.queryForList("SELECT performance_id, title FROM performance"));
        model.addAttribute("workerList", jdbc.queryForList("SELECT worker_id, fio FROM worker"));
        model.addAttribute("requisiteList", jdbc.queryForList("SELECT requisite_id, title FROM requisite"));
        model.addAttribute("isAdmin", true);
        return "repertoire_edit";
    }

    @PostMapping
    @Transactional
    public String add(@RequestParam Long performanceId,
                      @RequestParam String date,
                      @RequestParam String time,
                      @RequestParam(required = false) List<Long> workerIds,
                      @RequestParam(required = false) List<Long> reqIds,
                      @RequestParam(required = false) List<Integer> qtys,
                      HttpSession session,
                      RedirectAttributes ra) {
        if (!isAdmin(session)) {
            return "redirect:/repertoire?error=accessDenied";
        }

        // Вставка в таблицу repertoire
        jdbc.update("INSERT INTO repertoire(performance_id, date, time) VALUES (?, ?, ?)",
                performanceId, Date.valueOf(LocalDate.parse(date)), Time.valueOf(LocalTime.parse(time)));

        Long repId = jdbc.queryForObject(
                "SELECT currval(pg_get_serial_sequence('repertoire','repertoire_id'))", Long.class);

        // Вставка связей с работниками
        if (workerIds != null) {
            for (Long wId : workerIds) {
                if (wId != null) {
                    jdbc.update("INSERT INTO repertoire_worker(repertoire_id, worker_id) VALUES (?, ?)", repId, wId);
                }
            }
        }

        // Проверка и вставка реквизита
        if (reqIds != null && qtys != null) {
            if (reqIds.size() != qtys.size()) {
                ra.addFlashAttribute("error", "Ошибка: количество реквизитов и количеств не совпадает.");
                return "redirect:/repertoire";
            }

            for (int i = 0; i < reqIds.size(); i++) {
                Long rId = reqIds.get(i);
                Integer needObj = qtys.get(i);

                if (rId == null || needObj == null) {
                    // Пропускаем пустые записи или можно сообщить ошибку
                    ra.addFlashAttribute("error", "Ошибка: переданы пустые значения реквизита или количества.");
                    return "redirect:/repertoire";
                }

                int need = needObj;

                Integer avail = jdbc.queryForObject(
                        "SELECT available_quantity FROM requisite WHERE requisite_id = ?", Integer.class, rId);

                if (avail == null) {
                    ra.addFlashAttribute("error", "Ошибка: реквизит ID=" + rId + " не найден.");
                    return "redirect:/repertoire";
                }

                if (avail < need) {
                    ra.addFlashAttribute("stockError",
                            "Не хватает реквизита ID=" + rId + ": в наличии " + avail + ", нужно " + need);
                    return "redirect:/repertoire";
                }

                // Вставка в repertoire_requisite
                jdbc.update("INSERT INTO repertoire_requisite(repertoire_id, requisite_id, quantity) VALUES (?, ?, ?)",
                        repId, rId, need);

                // Вставка в conditions
                jdbc.queryForObject("""
                    INSERT INTO conditions(date, condition_type_id, requisite_id, quantity, comment)
                    VALUES (?, ?, ?, ?, ?)
                    RETURNING condition_id
                    """, Long.class, Date.valueOf(LocalDate.now()), 1, rId, need, "Задействовано в репертуаре");

                // Обновление available_quantity
                jdbc.update("UPDATE requisite SET available_quantity = available_quantity - ? WHERE requisite_id = ?", need, rId);
            }
        }

        return "redirect:/repertoire";
    }

    @PostMapping("/edit/{id}")
    @Transactional
    public String edit(@PathVariable Long id,
                       @RequestParam Long performanceId,
                       @RequestParam String date,
                       @RequestParam String time,
                       @RequestParam(required = false) List<Long> workerIds,
                       @RequestParam(required = false) List<Long> reqIds,
                       @RequestParam(required = false) List<Integer> qtys,
                       HttpSession session,
                       RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/repertoire?error=accessDenied";

        List<Map<String, Object>> oldReqs = jdbc.queryForList(
                "SELECT requisite_id, quantity FROM repertoire_requisite WHERE repertoire_id = ?", id);
        for (Map<String, Object> row : oldReqs) {
            Long reqId = ((Number) row.get("requisite_id")).longValue();
            Integer qty = ((Number) row.get("quantity")).intValue();
            jdbc.update("UPDATE requisite SET available_quantity = available_quantity + ? WHERE requisite_id = ?", qty, reqId);
        }

        jdbc.update("UPDATE repertoire SET performance_id=?, date=?, time=? WHERE repertoire_id=?",
                performanceId, Date.valueOf(LocalDate.parse(date)), Time.valueOf(LocalTime.parse(time)), id);

        jdbc.update("DELETE FROM repertoire_worker WHERE repertoire_id=?", id);
        jdbc.update("DELETE FROM repertoire_requisite WHERE repertoire_id=?", id);

        if (workerIds != null) {
            workerIds.forEach(w -> jdbc.update("INSERT INTO repertoire_worker(repertoire_id, worker_id) VALUES (?, ?)", id, w));
        }

        if (reqIds != null && qtys != null) {
            for (int i = 0; i < reqIds.size(); i++) {
                long rId = reqIds.get(i);
                int need = qtys.get(i);
                Integer avail = jdbc.queryForObject("SELECT available_quantity FROM requisite WHERE requisite_id = ?", Integer.class, rId);
                if (avail == null || avail < need) {
                    ra.addFlashAttribute("stockError", "Не хватает реквизита ID=" + rId + ": в наличии " + avail + ", нужно " + need);
                    throw new RuntimeException("Недостаточно реквизита");
                }

                jdbc.update("INSERT INTO repertoire_requisite(repertoire_id, requisite_id, quantity) VALUES (?, ?, ?)", id, rId, need);

                jdbc.queryForObject("""
                    INSERT INTO conditions(date, condition_type_id, requisite_id, quantity, comment)
                    VALUES (?, ?, ?, ?, ?)
                    RETURNING condition_id
                """, Long.class, Date.valueOf(LocalDate.now()), 2, rId, need, "Обновлено в репертуаре");

                jdbc.update("UPDATE requisite SET available_quantity = available_quantity - ? WHERE requisite_id = ?", need, rId);
            }
        }

        return "redirect:/repertoire";
    }

    @GetMapping("/delete/{id}")
    @Transactional
    public String delete(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/repertoire?error=accessDenied";

        List<Map<String, Object>> oldReqs = jdbc.queryForList(
                "SELECT requisite_id, quantity FROM repertoire_requisite WHERE repertoire_id = ?", id);
        for (Map<String, Object> row : oldReqs) {
            Long reqId = ((Number) row.get("requisite_id")).longValue();
            Integer qty = ((Number) row.get("quantity")).intValue();
            jdbc.update("UPDATE requisite SET available_quantity = available_quantity + ? WHERE requisite_id = ?", qty, reqId);
        }

        jdbc.update("DELETE FROM repertoire_worker WHERE repertoire_id=?", id);
        jdbc.update("DELETE FROM repertoire_requisite WHERE repertoire_id=?", id);
        jdbc.update("DELETE FROM repertoire WHERE repertoire_id=?", id);

        return "redirect:/repertoire";
    }
}
