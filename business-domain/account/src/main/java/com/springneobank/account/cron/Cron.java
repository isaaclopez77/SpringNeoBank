package com.springneobank.account.cron;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.scheduling.annotation.Scheduled;

import com.springneobank.account.entities.Account;

public class Cron {
    /*@Scheduled(cron = "0 0 0 * * *") // todos los días a medianoche
    public void processDailyInterest() {
        var accounts = accountRepository.findSavingsAccounts();

        for (Account acc : accounts) {
            BigDecimal rate = acc.getAccountType().getInterestRate();
            BigDecimal dailyInterest = acc.getBalance()
                    .multiply(rate)
                    .divide(BigDecimal.valueOf(365), RoundingMode.HALF_UP);

            if (dailyInterest.compareTo(BigDecimal.ZERO) > 0) {
                acc.setBalance(acc.getBalance().add(dailyInterest));
                accountRepository.save(acc);

                // Crear transacción de "interest"
                transactionClient.registerInterest(acc.getId(), dailyInterest);
            }
        }
    }*/

}
