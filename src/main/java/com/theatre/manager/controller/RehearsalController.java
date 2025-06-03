package com.theatre.manager.controller;

import com.theatre.manager.dto.RehearsalDto;
import com.theatre.manager.dto.WorkerDto;
import com.theatre.manager.service.RehearsalService;
import com.theatre.manager.service.WorkerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/rehearsals")
public class RehearsalController {
    private final RehearsalService rehearsalService;
    private final WorkerService workerService;

    public RehearsalController(RehearsalService rehearsalService, WorkerService workerService) {
        this.rehearsalService = rehearsalService;
        this.workerService = workerService;
    }

    private boolean isAdminOrDirector(HttpSession session) {
        String role = (String) session.getAttribute("workerRole");
        return role != null
                && (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("director"));
    }

    // 1) Страница «список репетиций»
    @GetMapping
    public String showRehearsalsPage(Model model, HttpSession session) {
        List<RehearsalDto> rehearsals = rehearsalService.getAllRehearsals();
        model.addAttribute("rehearsals", rehearsals);
        model.addAttribute("canAdd", isAdminOrDirector(session));
        return "rehearsals";
    }


    // 2) GET-форма «Добавить репетицию»
    @GetMapping("/add")
    public String addRehearsalForm(Model model, HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/rehearsals?error=noRights";
        }

        // Создаём пустой DTO
        RehearsalDto rehearsal = new RehearsalDto();
        rehearsal.setWorkers(List.of()); // пустой список актёров

        model.addAttribute("rehearsal", rehearsal);
        // Получаем только актёров (WorkerDto с id + fio)
        List<WorkerDto> actors = workerService.getWorkersByPostDto("Актёр");
        model.addAttribute("allActors", actors);

        model.addAttribute("isNew", true);
        return "rehearsal-form";
    }


    // 3) POST — «Сохранить новую репетицию»
    @PostMapping("/add")
    public String addRehearsal(
            @RequestParam("date") String dateStr,
            @RequestParam("time") String timeStr,
            @RequestParam("room") String room,
            @RequestParam("comment") String comment,
            @RequestParam("actorIds") List<Long> actorIds,   // здесь явно actorIds
            HttpSession session
    ) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/rehearsals?error=noRights";
        }
        LocalDate date = LocalDate.parse(dateStr);
        LocalTime time = LocalTime.parse(timeStr);

        // Передаём именно список actorIds
        rehearsalService.addRehearsal(date, time, room, comment, actorIds);
        return "redirect:/rehearsals";
    }


    // 4) GET-форма «Редактировать репетицию»
    @GetMapping("/edit/{id}")
    public String editRehearsalForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/rehearsals?error=noRights";
        }
        RehearsalDto rehearsal = rehearsalService.getRehearsalById(id);
        if (rehearsal == null) {
            return "redirect:/rehearsals?error=notFound";
        }
        model.addAttribute("rehearsal", rehearsal);

        // Получаем **только актёров** в DTO
        List<WorkerDto> actors = workerService.getWorkersByPostDto("Актёр");
        model.addAttribute("allActors", actors);

        model.addAttribute("isNew", false);
        return "rehearsal-form";
    }


    // 5) POST — «Сохранить изменения в репетиции»
    @PostMapping("/update")
    public String updateRehearsal(
            @RequestParam("rehearsalId") Long rehearsalId,
            @RequestParam("date") String dateStr,
            @RequestParam("time") String timeStr,
            @RequestParam("room") String room,
            @RequestParam("comment") String comment,
            @RequestParam(name = "actorIds", required = false) List<Long> actorIds,
            HttpSession session
    ) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/rehearsals?error=noRights";
        }
        LocalDate date = LocalDate.parse(dateStr);
        LocalTime time = LocalTime.parse(timeStr);

        // Передаём список actorIds в сервис
        rehearsalService.updateRehearsal(rehearsalId, date, time, room, comment, actorIds);
        return "redirect:/rehearsals";
    }


    // 6) POST — «Удалить репетицию»
    @PostMapping("/delete/{id}")
    public String deleteRehearsal(@PathVariable Long id, HttpSession session) {
        if (!isAdminOrDirector(session)) {
            return "redirect:/rehearsals?error=noRights";
        }
        rehearsalService.deleteRehearsal(id);
        return "redirect:/rehearsals";
    }
}
