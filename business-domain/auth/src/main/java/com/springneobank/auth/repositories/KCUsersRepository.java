package com.springneobank.auth.repositories;

import org.springframework.stereotype.Repository;

import com.springneobank.auth.entities.KCUser;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface KCUsersRepository extends JpaRepository<KCUser, Long>{

    public Optional<KCUser> findByKeycloakID(UUID keycloakID);
}
