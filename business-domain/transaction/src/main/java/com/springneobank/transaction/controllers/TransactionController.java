package com.springneobank.transaction.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springneobank.transaction.common.OperationResult;
import com.springneobank.transaction.services.TransactionService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/transaction")
@Tag(name = "Transaction API")
public class TransactionController {

    @Autowired
    private TransactionService tService;

    @Autowired HttpServletRequest request;

    @GetMapping("/get/account/{account_id}")
    public ResponseEntity<?> getTransactions(@PathVariable("account_id") Long accountID) {

        OperationResult<?> response = tService.getTransactions(accountID);

        if(!response.isSuccess()) {
            return ResponseEntity.badRequest().body(Map.of("message", response.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", response.getData()));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> createWithdrawTransaction(@RequestParam("source_account") Long sourceId,
                                                    @RequestParam("amount") BigDecimal amount,
                                                    @RequestParam("description") String description) {

        OperationResult<?> response = tService.withdraw(sourceId, amount, description);

        if(!response.isSuccess()) {
            return ResponseEntity.badRequest().body(Map.of("message", response.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", response.getData()));
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> createDepositTransaction(@RequestParam("target_account") Long targetId,
                                                    @RequestParam("amount") BigDecimal amount,
                                                    @RequestParam("description") String description) {

        OperationResult<?> response = tService.deposit(targetId, amount, description);

        if(!response.isSuccess()) {
            return ResponseEntity.badRequest().body(Map.of("message", response.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", response.getData()));
    }

    @PostMapping("/transference")
    public ResponseEntity<?> createTransferenceTransaction(@RequestParam("target_account") Long targetId,
                                                    @RequestParam("source_account") Long sourceId,
                                                    @RequestParam("amount") BigDecimal amount,
                                                    @RequestParam("description") String description) {

        OperationResult<?> response = tService.transfer(sourceId, targetId, amount, description);

        if(!response.isSuccess()) {
            return ResponseEntity.badRequest().body(Map.of("message", response.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", response.getData()));
    }
    
}
