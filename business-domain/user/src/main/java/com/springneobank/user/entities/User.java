package com.springneobank.user.entities;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class User {
    @Id
    private Long id;
    private String phone;
    private boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(Long id, String phone, boolean status) {
        this.id = id;
        this.phone = phone;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}