package com.theatre.manager.controller;

import com.theatre.manager.dao.ConditionDao;
import com.theatre.manager.dao.RequisiteDao;
import com.theatre.manager.dto.RepertoireRequisiteDto;
import com.theatre.manager.entity.Requisite;
import com.theatre.manager.enums.WorkerRole;
import com.theatre.manager.model.Condition;
import com.theatre.manager.model.ConditionType;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class ConditionController {
    private final ConditionDao conditionDao;
    private final RequisiteDao requisiteDao;
    private final JdbcTemplate jdbcTemplate;

    public ConditionController(ConditionDao conditionDao, RequisiteDao requisiteDao, JdbcTemplate jdbcTemplate) {
        this.conditionDao = conditionDao;
        this.requisiteDao = requisiteDao;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/conditions")
    public String showConditions(Model model, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN, WorkerRole.DECORATOR)) {
            return "redirect:/login?error=access_denied";
        }

        try {
            // 1. Get all requisites
            List<Map<String, Object>> requisites = jdbcTemplate.queryForList(
                    "SELECT requisite_id, title, available_quantity FROM requisite");

            List<Condition> conditions = jdbcTemplate.query(
                    "SELECT c.condition_id, c.requisite_id, r.title as requisite_title, " +
                            "c.condition_type_id, ct.condition_type_name, " +
                            "c.quantity, c.comment, c.date, " +
                            "COALESCE(p.title, '') as performance_title " +
                            "FROM conditions c " +
                            "JOIN requisite r ON c.requisite_id = r.requisite_id " +
                            "JOIN condition_type ct ON c.condition_type_id = ct.condition_type_id " +
                            "LEFT JOIN repertoire_requisite rr ON c.comment LIKE 'Задействован в репертуаре ID %' " +
                            "LEFT JOIN repertoire rep ON c.comment LIKE 'Задействован в репертуаре ID %' AND " +
                            "rep.repertoire_id = CASE WHEN c.comment ~ '^Задействован в репертуаре ID \\d+$' THEN " +
                            "CAST(SUBSTRING(c.comment, 26) AS INTEGER) ELSE NULL END " +
                            "LEFT JOIN performance p ON rep.performance_id = p.performance_id " +
                            "ORDER BY c.date DESC",
                    (rs, rowNum) -> {
                        Condition condition = new Condition();
                        condition.setConditionId(rs.getLong("condition_id"));
                        condition.setRequisiteId(rs.getLong("requisite_id"));
                        condition.setRequisiteTitle(rs.getString("requisite_title"));
                        condition.setConditionTypeId(rs.getInt("condition_type_id"));
                        condition.setConditionTypeName(rs.getString("condition_type_name"));
                        condition.setQuantity(rs.getInt("quantity"));
                        condition.setComment(rs.getString("comment"));
                        condition.setDate(rs.getDate("date").toLocalDate());
                        condition.setPerformanceTitle(rs.getString("performance_title"));
                        return condition;
                    });
            // 3. Get requisites used in repertoire (simplified)
            List<RepertoireRequisiteDto> usedRequisites = jdbcTemplate.query(
                    "SELECT r.title as requisite_title, p.title as performance_title, " +
                            "rr.quantity, rp.date, rp.time " +
                            "FROM repertoire_requisite rr " +
                            "JOIN requisite r ON rr.requisite_id = r.requisite_id " +
                            "JOIN repertoire rp ON rr.repertoire_id = rp.repertoire_id " +
                            "JOIN performance p ON rp.performance_id = p.performance_id " +
                            "ORDER BY rp.date DESC, rp.time DESC",
                    (rs, rowNum) -> {
                        RepertoireRequisiteDto dto = new RepertoireRequisiteDto();
                        dto.setRequisiteTitle(rs.getString("requisite_title"));
                        dto.setPerformanceTitle(rs.getString("performance_title"));
                        dto.setQuantity(rs.getInt("quantity"));
                        dto.setDate(rs.getDate("date").toLocalDate());
                        dto.setTime(rs.getTime("time").toLocalTime());
                        return dto;
                    });

            model.addAttribute("requisites", requisites);
            model.addAttribute("conditions", conditions);
            model.addAttribute("usedRequisites", usedRequisites);
            model.addAttribute("conditionTypes", conditionDao.findAllConditionTypes());
            model.addAttribute("isAdminOrDecorator", hasAccess(session, WorkerRole.ADMIN, WorkerRole.DECORATOR));
            model.addAttribute("isAdmin", hasAccess(session, WorkerRole.ADMIN));

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при загрузке данных: " + e.getMessage());
            e.printStackTrace(); // Добавьте логирование ошибки
        }

        return "conditions";
    }

    @GetMapping("/conditions/types")
    @ResponseBody
    public List<ConditionType> getAllConditionTypes() {
        return conditionDao.findAllConditionTypes();
    }

    @GetMapping("/conditions/with-titles")
    public String getConditionsWithTitles(Model model) {
        model.addAttribute("conditions", conditionDao.findAllWithRequisiteTitles());
        return "conditions-list"; // Имя вашего Thymeleaf шаблона
    }
    @PostMapping("/condition/save")
    public String saveCondition(@ModelAttribute Condition condition, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN, WorkerRole.DECORATOR)) {
            return "redirect:/conditions?error=access_denied";
        }

        try {
            // Обновляем available_quantity в requisite
            if (condition.getConditionTypeId() == 7) { // Задействован
                jdbcTemplate.update(
                        "UPDATE requisite SET available_quantity = available_quantity - ? WHERE requisite_id = ?",
                        condition.getQuantity(), condition.getRequisiteId());
            } else {
                // Для других типов условий (например, списание)
                jdbcTemplate.update(
                        "UPDATE requisite SET available_quantity = available_quantity - ? WHERE requisite_id = ?",
                        condition.getQuantity(), condition.getRequisiteId());
            }

            // Сохраняем запись в conditions
            condition.setDate(LocalDate.now());
            conditionDao.addCondition(condition);

            return "redirect:/conditions?success=true";
        } catch (Exception e) {
            return "redirect:/conditions?error=save_failed";
        }
    }

    @PostMapping("/condition/update")
    public String updateCondition(@ModelAttribute Condition condition, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN)) {
            return "redirect:/conditions?error=access_denied";
        }

        try {
            Condition oldCondition = conditionDao.findById(condition.getConditionId());

            // Возвращаем старое количество
            jdbcTemplate.update(
                    "UPDATE requisite SET available_quantity = available_quantity + ? WHERE requisite_id = ?",
                    oldCondition.getQuantity(), oldCondition.getRequisiteId());

            // Применяем новое количество
            jdbcTemplate.update(
                    "UPDATE requisite SET available_quantity = available_quantity - ? WHERE requisite_id = ?",
                    condition.getQuantity(), condition.getRequisiteId());

            conditionDao.updateCondition(condition);
            return "redirect:/conditions?success=true";
        } catch (Exception e) {
            return "redirect:/conditions?error=update_failed";
        }
    }

    @GetMapping("/condition/delete/{conditionId}")
    public String deleteCondition(@PathVariable Long conditionId, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN)) {
            return "redirect:/conditions?error=access_denied";
        }

        try {
            Condition condition = conditionDao.findById(conditionId);
            if (condition != null) {
                // Возвращаем количество
                jdbcTemplate.update(
                        "UPDATE requisite SET available_quantity = available_quantity + ? WHERE requisite_id = ?",
                        condition.getQuantity(), condition.getRequisiteId());

                conditionDao.deleteById(conditionId);
            }
            return "redirect:/conditions?success=true";
        } catch (Exception e) {
            return "redirect:/conditions?error=delete_failed";
        }
    }

    private boolean hasAccess(HttpSession session, WorkerRole... roles) {
        Object roleAttr = session.getAttribute("workerRole");
        if (roleAttr == null) return false;

        String role = roleAttr.toString();
        for (WorkerRole r : roles) {
            if (r.name().equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
}