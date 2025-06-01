package com.theatre.manager.controller;

import com.theatre.manager.entity.Worker;
import com.theatre.manager.repository.WorkerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private WorkerRepository workerRepository;

    @GetMapping("/login")
    public String loginForm() {
        return "login"; // страница login.html
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String login,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Worker worker = workerRepository.findByLogin(login);

        if (worker == null || !worker.getPassword().equals(password)) {
            model.addAttribute("error", "Неверный логин или пароль");
            return "login";
        }

        // Устанавливаем данные пользователя в сессию
        session.setAttribute("workerId", worker.getId());
        session.setAttribute("workerRole", worker.getRole()); // обязательно маленькими буквами
        session.setAttribute("workerFio", worker.getFio());

        return "redirect:/";  // после успешного логина на главную
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
