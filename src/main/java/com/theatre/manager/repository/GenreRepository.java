package com.theatre.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.theatre.manager.entity.Genre; //

public interface GenreRepository extends JpaRepository<Genre, Long> {
}
