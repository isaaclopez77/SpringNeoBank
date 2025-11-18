package com.springneobank.user.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springneobank.user.common.OperationResult;
import com.springneobank.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserService uService;

    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(
                                    @RequestHeader("Authorization") String authHeader,
                                    @RequestParam("email") String email,
                                    @RequestParam("name") String name,
                                    @RequestParam("lastName") String lastName,
                                    @RequestParam("password") String password,
                                    @RequestParam("phone") String phone) {


        OperationResult <?> result = uService.updateProfile(email, name, lastName, password, phone);

        if(!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result.getMessage());
        }

        return ResponseEntity.ok(result.getData());
    }
    
}
