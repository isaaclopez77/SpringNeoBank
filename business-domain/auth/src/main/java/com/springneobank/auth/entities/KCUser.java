package com.springneobank.auth.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="kc_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KCUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID keycloakID;
    private boolean status;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public KCUser(UUID keycloakID, boolean status) {
        this.keycloakID = keycloakID;
        this.status = status;
        this.created_at = LocalDateTime.now();
        this.updated_at = LocalDateTime.now();
    }
}
