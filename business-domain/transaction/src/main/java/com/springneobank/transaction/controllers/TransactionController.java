package com.springneobank.transaction.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springneobank.transaction.common.OperationResult;
import com.springneobank.transaction.services.TransactionService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


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
    
}
