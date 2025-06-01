package com.theatre.manager.controller;

import com.theatre.manager.dao.ConditionDao;
import com.theatre.manager.dao.RequisiteDao;
import com.theatre.manager.entity.ConditionType;
import com.theatre.manager.entity.Requisite;
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

    /**
     * 1) GET /condition/edit/{id} — форма «Добавить/редактировать состояние» для конкретного реквизита (ADMIN или DRESSER).
     */
    @GetMapping("/condition/edit/{id}")
    public String showEditConditionForm(@PathVariable Long id, Model model, HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (!"admin".equals(role) && !"dresser".equals(role)) {
            return "redirect:/requisites?error=access_denied&tab=requisites";
        }

        // Получаем сам реквизит
        Requisite requisite = requisiteDao.findById(id);
        if (requisite == null) {
            return "redirect:/requisites?error=not_found&tab=requisites";
        }

        // Список всех типов состояний
        List<ConditionType> conditionTypes = conditionDao.findAllConditionTypes();

        // Создаём пустой объект Condition, где будет заполнено лишь поле requisiteId
        Condition condition = new Condition();
        condition.setRequisiteId(id);

        model.addAttribute("requisite", requisite);
        model.addAttribute("conditionTypes", conditionTypes);
        model.addAttribute("condition", condition);
        return "condition-form";
    }

    /**
     * 2) POST /condition/save — сохраняем новую запись «состояние» и корректируем количество в реквизите (ADMIN или DRESSER).
     */
    @PostMapping("/condition/save")
    public String saveCondition(@ModelAttribute("condition") Condition condition,
                                HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (!"admin".equals(role) && !"dresser".equals(role)) {
            return "redirect:/requisites?error=access_denied&tab=requisites";
        }

        try {
            conditionDao.addConditionAndUpdateRequisite(condition);
            // После успешного добавления возвращаемся на вкладку «Состояния»
            return "redirect:/requisites?tab=conditions";
        } catch (RuntimeException e) {
            // Если, например, не хватает доступного количества — обратно на вкладку «Реквизиты» с ошибкой
            return "redirect:/requisites?error=not_enough&tab=requisites";
        }
    }

}
