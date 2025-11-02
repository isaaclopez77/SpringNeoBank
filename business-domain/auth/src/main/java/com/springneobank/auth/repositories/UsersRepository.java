package com.springneobank.auth.repositories;

import org.springframework.stereotype.Repository;

import com.springneobank.auth.entities.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UsersRepository extends JpaRepository<User, Long>{

    public Optional<User> findByKeycloakID(UUID keycloakID);
}
