package com.springneobank.account.cron;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.springneobank.account.entities.Account;
import com.springneobank.account.repositories.AccountRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InterestScheduler {

    @Autowired
    private AccountRepository accRepo;

    @Scheduled(cron = "0 0 0 * * *") // todos los días a medianoche
    @Transactional
    public void processDailyInterest() {
        log.info("CRON: starting... ");

        List<Account> accounts = accRepo.findSavingsAccounts();

        for (Account acc : accounts) {
            BigDecimal balance = acc.getBalance();
            log.info("CRON: balance: " + balance);
            BigDecimal rate = acc.getType().getInterestRate();
            // Calculate
            BigDecimal dailyInterest = balance.multiply(rate).divide(BigDecimal.valueOf(365), RoundingMode.HALF_UP);
            log.info("CRON: daily interest: " + dailyInterest);

            if (dailyInterest.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal newBalance = acc.getBalance().add(dailyInterest);
                acc.setBalance(newBalance);
                log.info("CRON: New balance: " + newBalance);
                accRepo.save(acc);

                // opt 2: Create transaction in transactions service
                //transactionClient.registerInterest(acc.getId(), dailyInterest);                
            }
        }
    }
}
