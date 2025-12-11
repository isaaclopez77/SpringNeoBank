package com.springneobank.transaction.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springneobank.transaction.entities.TransactionStatus;

@Repository
public interface TransactionStatusRepository  extends JpaRepository<TransactionStatus, Long>{

}
