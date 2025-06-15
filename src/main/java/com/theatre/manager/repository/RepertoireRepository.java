package com.theatre.manager.repository;

import com.theatre.manager.model.Repertoire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepertoireRepository extends JpaRepository<Repertoire, Long> {
}
