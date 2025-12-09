package com.springneobank.account.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="accounts")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
@ToString
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String iban;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AccountType type;
    private BigDecimal balance;
    private boolean status;

    @CreationTimestamp
    @Column(updatable=false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Account(Long userId, AccountType type, String iban) {
        this.userId = userId;
        this.iban = iban;
        this.balance = BigDecimal.ZERO;
        this.type = type;
        this.status = true;
    }
}
