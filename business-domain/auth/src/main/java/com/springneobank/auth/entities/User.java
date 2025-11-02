package com.springneobank.auth.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID keycloakID;
    private String email;
    private String name;
    private String lastName;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public User(UUID keycloakID, String email, String name, String lastName) {
        this.keycloakID = keycloakID;
        this.email = email;
        this.name = name;
        this.lastName = lastName;
        this.created_at = LocalDateTime.now();
        this.updated_at = LocalDateTime.now();
    }

    
}
