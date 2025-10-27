/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.springneobank.auth.controller;

import com.springneobank.auth.service.KeycloakRestService;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwk.Jwk;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.springneobank.auth.service.JwtService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.security.interfaces.RSAPublicKey;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
public class IndexController {

    private Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private KeycloakRestService restService;

    @Autowired
    private JwtService jwtService;

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
    
    @GetMapping("/valid")
    public ResponseEntity<?> valid(@RequestHeader("Authorization") String authHeader) throws Exception  {
        restService.checkValidity(authHeader);
        return ResponseEntity.ok(new HashMap (){{
            put("is_valid", "true");
        }});
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestParam("username") String username, @RequestParam("password") String password) {
        String login = restService.login(username, password);
       return ResponseEntity.ok(login);
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> logout(@RequestParam(value = "refresh_token", name = "refresh_token") String refreshToken) throws Exception  {
        restService.logout(refreshToken);
        return ResponseEntity.ok(new HashMap (){{
            put("logout", "true");
        }});
    }  
    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> refresh(@RequestParam(value = "refresh_token", name = "refresh_token") String refreshToken) throws Exception  {
        return ResponseEntity.ok(restService.refresh(refreshToken));
    }  
}
