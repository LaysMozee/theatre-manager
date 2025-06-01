package com.theatre.manager.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")  // <- вот тут надо обязательно!
    public String index(HttpSession session, Model model) {
        String username = (String) session.getAttribute("workerFio");
        String role = (String) session.getAttribute("workerRole");

        model.addAttribute("username", username != null ? username : "Гость");
        model.addAttribute("role", role != null ? role : "none");

        return "index";
    }

    @GetMapping("/repertoire")
    public String repertoire() {
        return "repertoire";
    }


    @GetMapping("/main-actors")
    public String actors(){
        return "actors";
    }
}
