/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.springneobank.auth.controller;

import com.springneobank.auth.service.KeycloakService;
import com.auth0.jwk.Jwk;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.springneobank.auth.entities.KCUser;
import com.springneobank.auth.messaging.UserRegisteredEvent;
import com.springneobank.auth.messaging.UserRegisteredPublisher;
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
public class AuthController {

    @Autowired
    private KeycloakService kcService;

    @Autowired
    private KCUserService databaseService;

    @Autowired
    private UserRegisteredPublisher urEventPublisher;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("name") String name,
            @RequestParam("lastName") String lastName,
            @RequestParam("password") String password,
            @RequestParam("phone") String phone) {
            
        // Create user in keycloack 
        UUID keycloakID = kcService.registerUser(username, password, email, name, lastName, phone);

        if(keycloakID != null) {
            // Create database relation
            KCUser kcUser = databaseService.createKCUser(keycloakID, username, password, email, name, lastName);
                
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

    @GetMapping("/roles")
    public ResponseEntity<?> getRoles(@RequestHeader("Authorization") String authHeader) throws Exception {
        DecodedJWT jwt = JWT.decode(authHeader.replace("Bearer", "").trim());

        // check JWT is valid
        Jwk jwk = jwtService.getJwk();
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

        algorithm.verify(jwt);

        // check JWT role is correct
        List<String> roles = ((List) jwt.getClaim("realm_access").asMap().get("roles"));

        // check JWT is still active
        Date expiryDate = jwt.getExpiresAt();
        if (expiryDate.before(new Date())) {
            throw new Exception("token is expired");
        }
        // all validation passed
        HashMap HashMap = new HashMap();
        for (String str : roles) {
            HashMap.put(str, str.length());
        }
        return ResponseEntity.ok(HashMap);
    }
}
