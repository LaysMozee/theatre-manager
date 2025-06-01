package com.theatre.manager.controller;

import com.theatre.manager.dao.RequisiteDao;
import com.theatre.manager.entity.Requisite;
import com.theatre.manager.model.ConditionView;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class RequisiteController {

    private final RequisiteDao requisiteDao;

    public RequisiteController(RequisiteDao requisiteDao) {
        this.requisiteDao = requisiteDao;
    }

    // ================ СПИСОК РЕКВИЗИТОВ: GET /requisites ================
    @GetMapping("/requisites")
    public String listRequisites(Model model, HttpSession session) {
        // Берём только те поля, что нужны:
        List<Requisite> requisites = requisiteDao.findAllRequisitesSimple();
        model.addAttribute("requisites", requisites);

        String userRole = (String) session.getAttribute("userRole");
        model.addAttribute("userRole", userRole);

        return "requisites";  // шаблон requisites.html
    }

    @GetMapping("/requisites/add")
    public String addRequisiteForm(Model model, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole) && !"DECORATOR".equals(userRole)) {
            return "redirect:/requisites?error=access_denied";
        }

        model.addAttribute("requisite", new Requisite());
        return "requisite-form";
    }

    @PostMapping("/requisites/save")
    public String saveRequisite(@ModelAttribute Requisite requisite, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole) && !"DECORATOR".equals(userRole)) {
            return "redirect:/requisites?error=access_denied";
        }

        requisiteDao.save(requisite);
        return "redirect:/requisites";
    }

    @GetMapping("/requisites/edit/{id}")
    public String editRequisiteForm(@PathVariable Long id, Model model, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole) && !"DECORATOR".equals(userRole)) {
            return "redirect:/requisites?error=access_denied";
        }

        Requisite requisite = requisiteDao.findById(id);
        if (requisite == null) {
            return "redirect:/requisites?error=not_found";
        }

        model.addAttribute("requisite", requisite);
        return "requisite-form";
    }

    @PostMapping("/requisites/update")
    public String updateRequisite(@ModelAttribute Requisite requisite, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole) && !"DECORATOR".equals(userRole)) {
            return "redirect:/requisites?error=access_denied";
        }

        requisiteDao.update(requisite);
        return "redirect:/requisites";
    }

    @GetMapping("/requisites/delete/{id}")
    public String deleteRequisite(@PathVariable Long id, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/requisites?error=access_denied";
        }

        requisiteDao.deleteById(id);
        return "redirect:/requisites";
    }

    // ============= СПИСОК «СОСТОЯНИЙ»: GET /conditions =============
    @GetMapping("/conditions")
    public String listConditions(Model model, HttpSession session) {
        List<ConditionView> conditions = requisiteDao.findAllConditions();
        model.addAttribute("conditions", conditions);

        String userRole = (String) session.getAttribute("userRole");
        model.addAttribute("userRole", userRole);

        return "conditions";
    }

    @GetMapping("/conditions/add")
    public String addConditionForm(Model model, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole) && !"DECORATOR".equals(userRole)) {
            return "redirect:/conditions?error=access_denied";
        }
        model.addAttribute("allRequisites", requisiteDao.findAllRequisitesSimple());
        // model.addAttribute("allConditionTypes", conditionTypeDao.findAll());
        return "condition-form";
    }

    @PostMapping("/conditions/save")
    public String saveCondition(
            @RequestParam Long requisiteId,
            @RequestParam Long conditionTypeId,
            @RequestParam("date") java.util.Date date,
            HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole) && !"DECORATOR".equals(userRole)) {
            return "redirect:/conditions?error=access_denied";
        }

        requisiteDao.saveCondition(requisiteId, conditionTypeId, date);
        return "redirect:/conditions";
    }

    @GetMapping("/conditions/edit/{id}")
    public String editConditionForm(@PathVariable Long id, Model model, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole) && !"DECORATOR".equals(userRole)) {
            return "redirect:/conditions?error=access_denied";
        }

        ConditionView cond = requisiteDao.findConditionById(id);
        if (cond == null) {
            return "redirect:/conditions?error=not_found";
        }

        model.addAttribute("condition", cond);
        model.addAttribute("allRequisites", requisiteDao.findAllRequisitesSimple());
        // model.addAttribute("allConditionTypes", conditionTypeDao.findAll());
        return "condition-form";
    }

    @PostMapping("/conditions/update")
    public String updateCondition(
            @RequestParam Long conditionId,
            @RequestParam Long conditionTypeId,
            @RequestParam("date") java.util.Date date,
            HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole) && !"DECORATOR".equals(userRole)) {
            return "redirect:/conditions?error=access_denied";
        }

        requisiteDao.updateCondition(conditionId, conditionTypeId, date);
        return "redirect:/conditions";
    }

    @GetMapping("/conditions/delete/{id}")
    public String deleteCondition(@PathVariable Long id, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/conditions?error=access_denied";
        }

        requisiteDao.deleteConditionById(id);
        return "redirect:/conditions";
    }
}
