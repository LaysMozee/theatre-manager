package com.theatre.manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "requisite")
public class Requisite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "requisite_id")
    private Long requisiteId;

    @Column(nullable = false)
    private String title;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "available_quantity")
    private Integer availableQuantity;

    private String size;
}