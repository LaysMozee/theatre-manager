package com.theatre.manager.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "rehearsal") // имя таблицы в БД должно быть в нижнем регистре
public class Rehearsal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rehearsal_id")
    private Long rehearsalId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "time", nullable = false)
    private LocalTime time;

    @Column(name = "rehearsal_room", nullable = false)
    private String rehearsalRoom;

    // --- Геттеры и сеттеры ---
    public Long getRehearsalId() {
        return rehearsalId;
    }

    public void setRehearsalId(Long rehearsalId) {
        this.rehearsalId = rehearsalId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getRehearsalRoom() {
        return rehearsalRoom;
    }

    public void setRehearsalRoom(String rehearsalRoom) {
        this.rehearsalRoom = rehearsalRoom;
    }
}
