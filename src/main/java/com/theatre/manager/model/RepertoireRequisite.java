package com.theatre.manager.model;

import com.theatre.manager.entity.Requisite;
import jakarta.persistence.*;

@Entity
@Table(name = "repertoire_requisite")
public class RepertoireRequisite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repertoire_requisite_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repertoire_id", nullable = false)
    private Repertoire repertoire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisite_id", nullable = false)
    private Requisite requisite;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Геттеры и сеттеры

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Repertoire getRepertoire() {
        return repertoire;
    }

    public void setRepertoire(Repertoire repertoire) {
        this.repertoire = repertoire;
    }

    public Requisite getRequisite() {
        return requisite;
    }

    public void setRequisite(Requisite requisite) {
        this.requisite = requisite;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
