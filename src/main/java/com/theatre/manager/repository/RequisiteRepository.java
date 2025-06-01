package com.theatre.manager.repository;

import com.theatre.manager.entity.Requisite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequisiteRepository extends JpaRepository<Requisite, Long> {
    // findAll здесь уже есть по умолчанию
}
