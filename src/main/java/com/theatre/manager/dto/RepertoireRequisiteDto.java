package com.theatre.manager.dto;

public class RepertoireRequisiteDto {
    private Long id;
    private Long repertoireId;
    private Long requisiteId;
    private int quantity;

    public RepertoireRequisiteDto() {}

    public RepertoireRequisiteDto(Long id, Long repertoireId, Long requisiteId, int quantity) {
        this.id = id;
        this.repertoireId = repertoireId;
        this.requisiteId = requisiteId;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRepertoireId() {
        return repertoireId;
    }

    public void setRepertoireId(Long repertoireId) {
        this.repertoireId = repertoireId;
    }

    public Long getRequisiteId() {
        return requisiteId;
    }

    public void setRequisiteId(Long requisiteId) {
        this.requisiteId = requisiteId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
