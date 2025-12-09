package com.springneobank.transaction.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springneobank.transaction.entities.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>{

    List<Transaction> findBySourceAccountIdOrTargetAccountId(Long sourceId, Long targetID);
}
