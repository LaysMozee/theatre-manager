package com.theatre.manager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "worker")
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

    @Column(name = "photo_filename")
    private String photoFilename;

    @Column(nullable = false)
    private String role;  // роли: "admin", "actor", "director", "costumer" и т.п.

    @Column(nullable = false)
    private String fio;

    private Integer age;

    private String post;

    @Column(name = "shoe_size")
    private Integer shoeSize;

    @Column(name = "clothing_size")
    private Integer clothingSize;

    private Integer experience;

    // Геттеры и сеттеры

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhotoFilename() {
        return photoFilename;
    }
    public void setPhotoFilename(String photoFilename) {
        this.photoFilename = photoFilename;
    }

    public String getRole() {
        return role == null ? "" : role;
    }
    public void setRole(String role) {
        this.role = role.toLowerCase(); // чтобы всегда хранить маленькими буквами
    }

    public String getFio() {
        return fio;
    }
    public void setFio(String fio) {
        this.fio = fio;
    }

    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPost() {
        return post;
    }
    public void setPost(String post) {
        this.post = post;
    }

    public Integer getShoeSize() {
        return shoeSize;
    }
    public void setShoeSize(Integer shoeSize) {
        this.shoeSize = shoeSize;
    }

    public Integer getClothingSize() {
        return clothingSize;
    }
    public void setClothingSize(Integer clothingSize) {
        this.clothingSize = clothingSize;
    }

    public Integer getExperience() {
        return experience;
    }
    public void setExperience(Integer experience) {
        this.experience = experience;
    }
}
