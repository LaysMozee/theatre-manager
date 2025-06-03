package com.theatre.manager.controller;

import com.theatre.manager.dao.ConditionDao;
import com.theatre.manager.dao.RequisiteDao;
import com.theatre.manager.entity.ConditionType;
import com.theatre.manager.entity.Requisite;
import com.theatre.manager.enums.WorkerRole;
import com.theatre.manager.model.Condition;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ConditionController {
    private final ConditionDao conditionDao;
    private final RequisiteDao requisiteDao;

    public ConditionController(ConditionDao conditionDao, RequisiteDao requisiteDao) {
        this.conditionDao = conditionDao;
        this.requisiteDao = requisiteDao;
    }

    @GetMapping("/condition/add")
    public String showAddConditionForm(Model model, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN, WorkerRole.DECORATOR)) {
            return "redirect:/requisites?error=access_denied&tab=conditions";
        }
        Condition condition = new Condition();
        List<Requisite> requisites = requisiteDao.findAllRequisitesSimple();
        List<ConditionType> conditionTypes = conditionDao.findAllConditionTypes();

        model.addAttribute("condition", condition);
        model.addAttribute("requisites", requisites);
        model.addAttribute("conditionTypes", conditionTypes);
        model.addAttribute("formAction", "/condition/save");
        model.addAttribute("workerRole", session.getAttribute("workerRole"));
        return "condition-form";
    }

    @GetMapping("/condition/requisite/{requisiteId}")
    public String showAddConditionForm(@PathVariable Long requisiteId, Model model, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN, WorkerRole.DECORATOR)) {
            return "redirect:/requisites?error=access_denied&tab=conditions";
        }
        Requisite requisite = requisiteDao.findById(requisiteId);
        if (requisite == null) {
            return "redirect:/requisites?error=not_found&tab=conditions";
        }
        Condition condition = new Condition();
        condition.setRequisiteId(requisiteId);

        model.addAttribute("condition", condition);
        model.addAttribute("requisite", requisite);
        model.addAttribute("conditionTypes", conditionDao.findAllConditionTypes());
        model.addAttribute("formAction", "/condition/save");
        model.addAttribute("workerRole", session.getAttribute("workerRole"));
        return "condition-form";
    }

    @GetMapping("/condition/edit/{conditionId}")
    public String showEditConditionForm(@PathVariable Long conditionId, Model model, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN, WorkerRole.DECORATOR)) {
            return "redirect:/requisites?error=access_denied&tab=conditions";
        }
        Condition condition = conditionDao.findById(conditionId);
        if (condition == null) {
            return "redirect:/requisites?error=not_found&tab=conditions";
        }
        Requisite requisite = requisiteDao.findById(condition.getRequisiteId());
        if (requisite == null) {
            return "redirect:/requisites?error=not_found&tab=conditions";
        }

        model.addAttribute("condition", condition);
        model.addAttribute("requisite", requisite);
        model.addAttribute("conditionTypes", conditionDao.findAllConditionTypes());
        model.addAttribute("formAction", "/condition/update");
        model.addAttribute("workerRole", session.getAttribute("workerRole"));
        return "condition-form";
    }

    @PostMapping("/condition/save")
    public String saveCondition(@ModelAttribute Condition condition, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN, WorkerRole.DECORATOR)) {
            return "redirect:/requisites?error=access_denied&tab=conditions";
        }
        try {
            conditionDao.addConditionAndUpdateRequisite(condition);
            return "redirect:/requisites?tab=conditions";
        } catch (RuntimeException e) {
            return "redirect:/requisites?error=save_failed&tab=conditions";
        }
    }

    @PostMapping("/condition/update")
    public String updateCondition(@ModelAttribute Condition condition, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN, WorkerRole.DECORATOR)) {
            return "redirect:/requisites?error=access_denied&tab=conditions";
        }
        try {
            conditionDao.updateCondition(condition);
            return "redirect:/requisites?tab=conditions";
        } catch (RuntimeException e) {
            return "redirect:/requisites?error=update_failed&tab=conditions";
        }
    }

    @GetMapping("/condition/delete/{conditionId}")
    public String deleteCondition(@PathVariable Long conditionId, HttpSession session) {
        if (!hasAccess(session, WorkerRole.ADMIN)) {
            return "redirect:/requisites?error=access_denied&tab=conditions";
        }
        try {
            conditionDao.deleteById(conditionId);
        } catch (RuntimeException ignored) {
        }
        return "redirect:/requisites?tab=conditions";
    }

    private WorkerRole getRoleFromSession(HttpSession session) {
        Object role = session.getAttribute("workerRole");
        if (role instanceof String) {
            try {
                return WorkerRole.valueOf(((String) role).toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }

    private boolean hasAccess(HttpSession session, WorkerRole... roles) {
        WorkerRole current = getRoleFromSession(session);
        if (current == null) return false;
        for (WorkerRole role : roles) {
            if (role == current) return true;
        }
        return false;
    }
}
