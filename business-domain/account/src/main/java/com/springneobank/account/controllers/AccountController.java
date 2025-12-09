package com.springneobank.account.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springneobank.account.common.OperationResult;
import com.springneobank.account.services.AccountService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/account")
@Tag(name = "Account API")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@RequestParam("account_type") Long typeID) {
        OperationResult<?> result = accountService.createAccount(typeID);

        if(!result.isSuccess()) {
            return ResponseEntity.internalServerError().body(Map.of("message", result.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", result.getData()));
    }

    @PostMapping("/change_status/{id}")
    public ResponseEntity<?> changeAccountStatus(@PathVariable("id") Long id, @RequestParam("iban") String iban, @RequestParam("status") boolean status) {
        OperationResult<?> result = accountService.changeAccountStatus(id, iban, status);

        if(!result.isSuccess()) {
            return ResponseEntity.internalServerError().body(Map.of("message", result.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", result.getData()));
    }

    @GetMapping("/get_user_accounts")
    public ResponseEntity<?> getUserAccounts() {
        OperationResult<?> result = accountService.getAccounts();

        if(!result.isSuccess()) {
            return ResponseEntity.internalServerError().body(Map.of("message", result.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", result.getData()));
    }
    
}