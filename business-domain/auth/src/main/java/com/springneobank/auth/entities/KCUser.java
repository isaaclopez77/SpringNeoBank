package com.springneobank.auth.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name="kc_users")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
@ToString
public class KCUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID keycloakID;
    private boolean status;

    @CreationTimestamp
    @Column(updatable=false)
    private LocalDateTime created_at;

    @UpdateTimestamp
    private LocalDateTime updated_at;

    public KCUser(UUID keycloakID, boolean status) {
        this.keycloakID = keycloakID;
        this.status = status;
    }
}
