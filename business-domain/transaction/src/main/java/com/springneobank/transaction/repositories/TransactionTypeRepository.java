package com.springneobank.transaction.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springneobank.transaction.entities.TransactionType;

@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, Long>{

} 
