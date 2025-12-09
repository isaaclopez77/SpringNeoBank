package com.springneobank.account.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springneobank.account.entities.AccountType;
import java.util.Optional;


@Repository
public interface AccountTypeRepository extends JpaRepository<AccountType, Long>{

    public Optional<AccountType> findById(Long id);
    
}