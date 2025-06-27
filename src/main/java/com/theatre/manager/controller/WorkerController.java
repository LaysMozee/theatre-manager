package com.theatre.manager.controller;

import com.theatre.manager.entity.Worker;
import com.theatre.manager.repository.WorkerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Controller
@RequestMapping("/workers")
public class WorkerController {

    private final WorkerRepository workerRepository;

    private final String uploadDir = "C:/Users/Алексей/Desktop/theatre-manager/uploads/photos/";

    @Autowired
    public WorkerController(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    // Отображение списка сотрудников
    @GetMapping
    public String listWorkers(Model model,
                              HttpSession session,
                              @RequestParam(required = false) String filter) {

        String currentUserRole = (String) session.getAttribute("workerRole");
        model.addAttribute("role", currentUserRole);
        model.addAttribute("isAdmin", "admin".equalsIgnoreCase(currentUserRole));

        List<Worker> workers;
        if (filter != null && !filter.isBlank()) {
            workers = workerRepository.findByFioContainingIgnoreCaseOrPostContainingIgnoreCase(filter, filter);
        } else {
            workers = workerRepository.findAll();
        }

        model.addAttribute("workers", workers);
        model.addAttribute("filter", filter != null ? filter : "");

        return "workers";
    }

    // Форма для добавления нового сотрудника (только для админа)
    @GetMapping("/add")
    public String addWorkerForm(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/workers";
        }
        model.addAttribute("worker", new Worker());
        model.addAttribute("formAction", "/workers/save");
        return "worker-form";
    }

    // Форма редактирования сотрудника (только для админа)
    @GetMapping("/edit/{id}")
    public String editWorkerForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/workers";
        }
        Worker worker = workerRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Сотрудник с ID " + id + " не найден"));
        model.addAttribute("worker", worker);
        model.addAttribute("formAction", "/workers/save");
        return "worker-form";
    }

    // Сохранение нового или обновление существующего сотрудника
    @PostMapping("/save")
    public String saveWorker(@ModelAttribute Worker worker,
                             @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
                             HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/workers";
        }

        try {
            if (worker.getId() != null) {
                // Обновляем существующего
                Worker existing = workerRepository.findById(worker.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Сотрудник не найден"));

                existing.setLogin(worker.getLogin());

                if (worker.getPassword() != null && !worker.getPassword().isEmpty()) {
                    existing.setPassword(worker.getPassword());
                }

                existing.setFio(worker.getFio());
                existing.setRole(worker.getRole() != null ? worker.getRole().toLowerCase() : null);
                existing.setAge(worker.getAge());
                existing.setPost(worker.getPost());
                existing.setShoeSize(worker.getShoeSize());
                existing.setClothingSize(worker.getClothingSize());
                existing.setExperience(worker.getExperience());

                if (photoFile != null && !photoFile.isEmpty()) {
                    String filename = savePhoto(photoFile);
                    existing.setPhotoFilename(filename);
                }

                workerRepository.save(existing);
            } else {
                // Новый сотрудник
                if (photoFile != null && !photoFile.isEmpty()) {
                    String filename = savePhoto(photoFile);
                    worker.setPhotoFilename(filename);
                }

                worker.setRole(worker.getRole() != null ? worker.getRole().toLowerCase() : null);

                workerRepository.save(worker);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Тут можно добавить логирование или показывать ошибку пользователю
        }

        return "redirect:/workers";
    }

    // Удаление сотрудника (только для админа)
    @PostMapping("/delete/{id}")
    public String deleteWorker(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/workers";
        }

        try {
            workerRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Нельзя удалить сотрудника, так как он участвует в репетициях или других мероприятиях. " +
                            "Сначала удалите все связанные записи.");
            return "redirect:/workers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Произошла ошибка при удалении сотрудника: " + e.getMessage());
            return "redirect:/workers";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Сотрудник успешно удален");
        return "redirect:/workers";
    }

    // Проверка, что пользователь — админ
    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("workerRole");
        return role != null && role.equalsIgnoreCase("admin");
    }

    //  фото на диск и возвращаем имя файла
    private String savePhoto(MultipartFile photoFile) throws IOException {
        String originalFilename = photoFile.getOriginalFilename();
        String filename = System.currentTimeMillis() + "_" +
                (originalFilename != null ? originalFilename.replaceAll("\\s+", "_") : "photo.jpg");

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        Files.copy(photoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }
}
