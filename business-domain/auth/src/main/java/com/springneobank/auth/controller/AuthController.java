/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.springneobank.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import com.springneobank.auth.common.OperationResult;
import com.springneobank.auth.common.Utils;
import com.springneobank.auth.service.JwtService;
import com.springneobank.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/auth")
@Tag(name = "Auth API")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private HttpServletRequest request;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("name") String name,
            @RequestParam("lastName") String lastName,
            @RequestParam("password") String password,
            @RequestParam("phone") String phone) {
            
        OperationResult <?> result = authService.registerUser(username, email, name, lastName, password, phone);

        if(!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result.getMessage());
        }

        return ResponseEntity.ok(result.getData());
    }

    @GetMapping("/validate_token")
    public ResponseEntity<?> validateToken() throws Exception {

        OperationResult <?> result = jwtService.validateToken(Utils.getAuthorizationHeader(request));

        if(!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result.getMessage());
        }

        return ResponseEntity.ok(result.getData());
    }
}
