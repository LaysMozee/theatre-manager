package com.theatre.manager.service;

import com.theatre.manager.entity.Rehearsal;
import com.theatre.manager.repository.RehearsalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RehearsalService {
    private final RehearsalRepository rehearsalRepository;

    public RehearsalService(RehearsalRepository rehearsalRepository) {
        this.rehearsalRepository = rehearsalRepository;
    }

    public List<Rehearsal> findAll() {
        return rehearsalRepository.findAll();
    }
}
