package com.theatre.manager.controller;

import com.theatre.manager.dao.RequisiteDao;
import com.theatre.manager.entity.Requisite;
import com.theatre.manager.enums.WorkerRole;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/requisites")
public class RequisiteController {

    private final RequisiteDao requisiteDao;
    private final JdbcTemplate jdbcTemplate; // Добавляем JdbcTemplate как поле класса

    public RequisiteController(RequisiteDao requisiteDao, JdbcTemplate jdbcTemplate) {
        this.requisiteDao = requisiteDao;
        this.jdbcTemplate = jdbcTemplate; // Инициализируем в конструкторе
    }


    // Обработка GET /requisites
    @GetMapping
    public String listRequisites(Model model, HttpSession session) {
        String roleStr = (String) session.getAttribute("workerRole");
        model.addAttribute("workerRole", roleStr != null ? roleStr.toLowerCase() : null);

        List<Requisite> requisites = requisiteDao.findAllRequisitesSimple();
        model.addAttribute("requisites", requisites);

        return "requisites"; // шаблон requisites.html
    }

    // Форма добавления нового реквизита (ADMIN и DECORATOR)
    @GetMapping("/add")
    public String addRequisiteForm(Model model, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN, WorkerRole.DECORATOR)) {
            return "redirect:/requisites?error=access_denied";
        }
        model.addAttribute("requisite", new Requisite());
        return "requisite-form";
    }

    // Сохранение нового реквизита
    @PostMapping("/save")
    public String saveRequisite(@ModelAttribute Requisite requisite, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN, WorkerRole.DECORATOR)) {
            return "redirect:/requisites?error=access_denied";
        }
        requisiteDao.save(requisite);
        return "redirect:/requisites";
    }

    // Форма редактирования существующего реквизита
    @GetMapping("/edit/{id}")
    public String editRequisiteForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN, WorkerRole.DECORATOR)) {
            return "redirect:/requisites?error=access_denied";
        }
        Requisite requisite = requisiteDao.findById(id);
        if (requisite == null) {
            return "redirect:/requisites?error=not_found";
        }
        model.addAttribute("requisite", requisite);
        return "requisite-form";
    }

    // Обновление реквизита
    @PostMapping("/update")
    public String updateRequisite(@ModelAttribute Requisite requisite, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN, WorkerRole.DECORATOR)) {
            return "redirect:/requisites?error=access_denied";
        }
        requisiteDao.update(requisite);
        return "redirect:/requisites";
    }

    @GetMapping("/delete/{id}")
    public String deleteRequisite(@PathVariable Long id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) { // Убираем JdbcOperations из параметров
        if (!hasAccess(session, WorkerRole.ADMIN, WorkerRole.DECORATOR)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Доступ запрещен");
            return "redirect:/requisites";
        }

        try {
            // Проверяем, используется ли реквизит в репертуаре
            Integer usageCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM repertoire_requisite WHERE requisite_id = ?",
                    Integer.class, id);

            if (usageCount != null && usageCount > 0) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Невозможно удалить реквизит, так как он используется в репертуаре. " +
                                "Сначала удалите его из всех мероприятий.");
                return "redirect:/requisites";
            }

            requisiteDao.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Реквизит успешно удален");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Невозможно удалить реквизит, так как он используется в системе. " +
                            "Сначала удалите все связанные записи.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Произошла ошибка при удалении реквизита: " + e.getMessage());
        }

        return "redirect:/requisites";
    }


    // Получение роли из сессии
    private WorkerRole getRoleFromSession(HttpSession session) {
        Object roleObj = session.getAttribute("workerRole");
        if (!(roleObj instanceof String)) return null;

        try {
            return WorkerRole.valueOf(((String) roleObj).toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Проверка доступа
    private boolean hasAccess(HttpSession session, WorkerRole... allowedRoles) {
        WorkerRole currentRole = getRoleFromSession(session);
        if (currentRole == null) return false;

        for (WorkerRole allowed : allowedRoles) {
            if (allowed == currentRole) return true;
        }
        return false;
    }
}
