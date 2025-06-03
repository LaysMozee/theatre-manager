package com.theatre.manager.repository;

import com.theatre.manager.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long> {

    Worker findByLogin(String login);

    List<Worker> findByRole(String role);

    @Override
    List<Worker> findAll();
    // Найти по ФИО или должности (по частичному совпадению, без учёта регистра)
    List<Worker> findByFioContainingIgnoreCaseOrPostContainingIgnoreCase(String fio, String post);
    List<Worker> findByPostIgnoreCase(String post);
    List<Worker> findByFioContainingIgnoreCase(String fioPart);

    List<Worker> findByPost(String post);
}