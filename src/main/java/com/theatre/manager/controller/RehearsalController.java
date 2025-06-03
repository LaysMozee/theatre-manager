package com.theatre.manager.controller;

import com.theatre.manager.dto.RehearsalDto;
import com.theatre.manager.dto.WorkerDto;
import com.theatre.manager.service.RehearsalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/rehearsals")
public class RehearsalController {

    private final RehearsalService rehearsalService;

    public RehearsalController(RehearsalService rehearsalService) {
        this.rehearsalService = rehearsalService;
    }

    /**
     * Вспомогательный метод: проверяет, является ли текущий пользователь
     * admin или director, исходя из session.getAttribute("workerRole").
     */
    private boolean isAdminOrDirector(HttpSession session) {
        String role = (String) session.getAttribute("workerRole");
        return role != null && (
                role.equalsIgnoreCase("admin") ||
                        role.equalsIgnoreCase("director")
        );
    }

    /**
     * GET /rehearsals — отображает страницу репетиций
     * В модель кладём:
     * 1) Список существующих репетиций с работниками (rehearsals)
     * 2) Список всех работников (allWorkers) для формы
     * 3) Флаг canAdd, показывающий, можно ли отображать форму добавления
     */
    @GetMapping
    public String showRehearsalsPage(Model model, HttpSession session) {
        // 1. Получаем все репетиции (связанное заполнение работников)
        List<RehearsalDto> rehearsals = rehearsalService.getAllRehearsals();
        model.addAttribute("rehearsals", rehearsals);

        // 2. Получаем список всех работников для мультиселекта
        List<WorkerDto> allWorkers = rehearsalService.getAllWorkers();
        model.addAttribute("allWorkers", allWorkers);

        // 3. Определяем, показывать ли форму добавления (только admin или director)
        boolean canAdd = isAdminOrDirector(session);
        model.addAttribute("canAdd", canAdd);

        return "rehearsals";  // thymeleaf-шаблон
    }

    /**
     * POST /rehearsals/add — обрабатывает отправку формы добавления новой репетиции
     */
    @PostMapping("/add")
    public String addRehearsal(
            @RequestParam("date") String dateStr,
            @RequestParam("time") String timeStr,
            @RequestParam("room") String room,
            @RequestParam("workerIds") List<Long> workerIds,
            HttpSession session
    ) {
        // Если роль не admin и не director — редиректим с параметром ошибки
        if (!isAdminOrDirector(session)) {
            return "redirect:/rehearsals?error=noRights";
        }

        // Парсим дату и время из строк
        LocalDate date = LocalDate.parse(dateStr);
        LocalTime time = LocalTime.parse(timeStr);

        // Добавляем репетицию в базу (связи в транзакции)
        rehearsalService.addRehearsal(date, time, room, workerIds);

        // После создания — редиректим обратно на страницу репетиций
        return "redirect:/rehearsals";
    }
}
