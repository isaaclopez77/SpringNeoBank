/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.springneobank.auth.controller;

import com.springneobank.auth.service.KeycloakService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import com.auth0.jwk.Jwk;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.springneobank.auth.common.Utils;
import com.springneobank.auth.entities.KCUser;
import com.springneobank.auth.messaging.UserRegistered.UserRegisteredEvent;
import com.springneobank.auth.messaging.UserRegistered.UserRegisteredPublisher;
import com.springneobank.auth.service.JwtService;
import com.springneobank.auth.service.KCUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/auth")
@Tag(name = "Auth API")
public class AuthController {

    @Autowired
    private KeycloakService kcService;

    @Autowired
    private KCUserService databaseService;

    @Autowired
    private UserRegisteredPublisher urEventPublisher;

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
            
        // Create user in keycloack 
        UUID keycloakID = kcService.registerUser(username, password, email, name, lastName);

        if(keycloakID != null) {
            // Create database relation
            KCUser kcUser = databaseService.createKCUser(keycloakID);
                
            // Register RabbitMQ event
            UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(kcUser.getId())
                .phone(phone)
                .build();

            urEventPublisher.publish(event);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/validate_token")
    public ResponseEntity<?> validateToken() throws Exception {
        try{
            String authHeader = Utils.getAuthorizationHeader(request);
            if(authHeader == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header not found");
            }

            String token = authHeader.replace("Bearer", "").trim();
            DecodedJWT jwt = JWT.decode(token);
            Jwk jwk = jwtService.getJwk();

            // Check JWT is valid
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            algorithm.verify(jwt);

            // Check JWT is still active
            Date expiryDate = jwt.getExpiresAt();
            if (expiryDate.before(new Date())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired");
            }
            return ResponseEntity.ok("Token valid");
        } catch(JWTVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body("Invalid token signature");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token validation failed: " + e.getMessage());
        }
    }
}
