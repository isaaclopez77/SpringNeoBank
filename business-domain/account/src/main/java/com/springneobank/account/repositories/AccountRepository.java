package com.springneobank.account.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.springneobank.account.entities.Account;
import java.util.List;


@Repository
public interface AccountRepository extends JpaRepository<Account, Long>{

    @Query("select max(a.id) from Account a")
    Optional<Long> getMaxAccountNumber();

    Optional<Account> findByUserIdAndIbanAndId(Long user_id, String iban, Long id);

    List<Account> findByUserIdAndStatusTrue(Long userId);
}