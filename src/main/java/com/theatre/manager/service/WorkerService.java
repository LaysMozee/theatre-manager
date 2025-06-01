package com.theatre.manager.service;

import com.theatre.manager.entity.Worker;
import com.theatre.manager.repository.WorkerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;

    public WorkerService(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    // Получить всех работников
    public List<Worker> findAll() {
        return workerRepository.findAll();
    }

    // Поиск по ФИО
    public List<Worker> findByFIO(String fio) {
        return workerRepository.findByFioContainingIgnoreCase(fio);
    }

    // Сохранить работника
    public Worker save(Worker worker) {
        return workerRepository.save(worker);
    }

    // Удалить по ID
    public void deleteById(Long id) {
        workerRepository.deleteById(id);
    }


}