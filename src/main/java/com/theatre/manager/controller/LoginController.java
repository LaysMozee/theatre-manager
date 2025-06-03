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

    // GET /login — показать форму логина
    @GetMapping("/login")
    public String loginForm() {
        return "login"; // thymeleaf-шаблон login.html
    }

    // POST /login — обработать отправку формы
    @PostMapping("/login")
    public String login(
            @RequestParam String login,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        Worker worker = workerRepository.findByLogin(login);
        // если нет такого пользователя или пароль не совпадает
        if (worker == null || !worker.getPassword().equals(password)) {
            model.addAttribute("error", "Неверный логин или пароль");
            return "login";
        }

        // Сохраняем в сессию: id, роль и ФИО
        session.setAttribute("workerId", worker.getId());
        session.setAttribute("workerRole", worker.getRole().toLowerCase()); // роль в нижнем регистре
        session.setAttribute("workerFio", worker.getFio());

        // После успешного логина перенаправляем на главную (HomeController переадресует на /rehearsals)
        return "redirect:/";
    }

    // GET /logout — выход из системы
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
