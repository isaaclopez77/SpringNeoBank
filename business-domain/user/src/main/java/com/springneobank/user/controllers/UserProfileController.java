package com.springneobank.user.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springneobank.user.common.OperationResult;
import com.springneobank.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserService uService;

    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(
                                    @RequestParam("email") String email,
                                    @RequestParam("name") String name,
                                    @RequestParam("lastName") String lastName,
                                    @RequestParam("phone") String phone) {


        OperationResult <?> result = uService.updateProfile(email, name, lastName, phone);

        if(!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result.getMessage());
        }

        return ResponseEntity.ok(result.getData());
    }

    @PostMapping("/change_password")
    public ResponseEntity<?> changePassword(@RequestParam("password") String password) {

        OperationResult <?> result = uService.changePassword(password);

        if(!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result.getMessage());
        }

        return ResponseEntity.ok(result.getData());
    }
    
}
