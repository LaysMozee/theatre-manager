package com.theatre.manager.controller;

import com.theatre.manager.dao.ConditionDao;
import com.theatre.manager.dao.RequisiteDao;
import com.theatre.manager.entity.Requisite;
import com.theatre.manager.enums.WorkerRole;
import com.theatre.manager.model.ConditionWithTitle;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/requisites") // общий префикс для всех методов
public class RequisiteController {

    private final RequisiteDao requisiteDao;
    private final ConditionDao conditionDao;

    public RequisiteController(RequisiteDao requisiteDao, ConditionDao conditionDao) {
        this.requisiteDao = requisiteDao;
        this.conditionDao = conditionDao;
    }

    // Обработка GET /requisites
    @GetMapping
    public String listRequisitesAndConditions(Model model, HttpSession session,
                                              @RequestParam(required = false) String tab) {
        String roleStr = (String) session.getAttribute("workerRole");
        model.addAttribute("workerRole", roleStr != null ? roleStr.toLowerCase() : null);

        // Получаем список реквизитов и условий для отображения
        List<Requisite> requisites = requisiteDao.findAllRequisitesSimple();
        model.addAttribute("requisites", requisites);

        List<ConditionWithTitle> conditions = conditionDao.findAllWithRequisiteTitles();
        model.addAttribute("conditions", conditions);

        // Выбираем активную вкладку (requisites или conditions)
        model.addAttribute("activeTab", "conditions".equals(tab) ? "conditions" : "requisites");

        return "requisites"; // thymeleaf шаблон requisites.html
    }

    // Форма добавления нового реквизита (доступно ADMIN и DECORATOR)
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

    // Удаление реквизита (только ADMIN)
    @GetMapping("/delete/{id}")
    public String deleteRequisite(@PathVariable Long id, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN)) {
            return "redirect:/requisites?error=access_denied";
        }
        requisiteDao.deleteById(id);
        return "redirect:/requisites";
    }

    // Вспомогательный метод для получения роли из сессии
    private WorkerRole getRoleFromSession(HttpSession session) {
        Object roleObj = session.getAttribute("workerRole");
        if (!(roleObj instanceof String)) return null;

        try {
            return WorkerRole.valueOf(((String) roleObj).toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Проверка доступа по ролям
    private boolean hasAccess(HttpSession session, WorkerRole... allowedRoles) {
        WorkerRole currentRole = getRoleFromSession(session);
        if (currentRole == null) return false;

        for (WorkerRole allowed : allowedRoles) {
            if (allowed == currentRole) return true;
        }
        return false;
    }
}