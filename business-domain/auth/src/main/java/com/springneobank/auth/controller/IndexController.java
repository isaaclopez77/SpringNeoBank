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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/auth")
public class IndexController {

    private Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private KeycloakRestService restService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam("username") String username, @RequestParam("email") String email, @RequestParam("password") String password) {
        restService.registerUser(username, email, password);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
