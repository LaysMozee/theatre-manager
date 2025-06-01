package com.theatre.manager.controller;

import com.theatre.manager.entity.Rehearsal;
import com.theatre.manager.repository.RehearsalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.theatre.manager.service.RehearsalService;

import java.util.List;

@Controller
public class RehearsalController {

    private final RehearsalService rehearsalService;

    // Внедрение через конструктор — лучше практика
    public RehearsalController(RehearsalService rehearsalService) {
        this.rehearsalService = rehearsalService;
    }

    @GetMapping("/rehearsals")
    public String showRehearsals(Model model) {
        List<Rehearsal> rehearsals = rehearsalService.findAll();
        model.addAttribute("rehearsals", rehearsals);
        return "rehearsals";
    }
}