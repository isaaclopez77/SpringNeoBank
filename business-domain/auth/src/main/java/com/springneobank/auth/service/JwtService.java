/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.springneobank.auth.service;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.springneobank.auth.common.OperationResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class JwtService {

    @Value("${keycloak.jwk-set-uri}")
    private String jwksUrl;

    @Cacheable(value = "jwkCache")
    public Jwk getJwk(String kid) throws Exception {
        URL url = new URL(jwksUrl);
        UrlJwkProvider urlJwkProvider = new UrlJwkProvider(url);
        Jwk get = urlJwkProvider.get(kid);  
        return get;
    }

    /**
     * Validate token function
     * 
     * @param authHeader -- Bearer *token*
     * @return
     */
    public OperationResult<String> validateToken(String authHeader) {
        try{
            if(authHeader == null) {
                return OperationResult.fail("Authorization header not found");
            }

            String token = authHeader.replace("Bearer", "").trim();
            DecodedJWT jwt = JWT.decode(token);
            String kid = jwt.getKeyId();
            Jwk jwk = getJwk(kid);

            // Check JWT is valid
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            algorithm.verify(jwt);

            // Check JWT is still active
            Date expiryDate = jwt.getExpiresAt();
            if (expiryDate.before(new Date())) {
                return OperationResult.fail("Token expired");
            }
            return OperationResult.ok("Token valid");
        } catch(JWTVerificationException e) {
            return OperationResult.fail("Invalid token signature");
        } catch(Exception e) {
            return OperationResult.fail("Token validation failed: " + e.getMessage());
        }
    }

    /**
     * Get roles from authorization header
     * 
     * @param authHeader  -- Bearer *token*
     * @return
     */
    public OperationResult<HashMap<String, Integer>> getRoles(String authHeader) {
        try{
            DecodedJWT jwt = JWT.decode(authHeader.replace("Bearer", "").trim());

            // Check JWT role is correct
            List<String> roles = ((List) jwt.getClaim("realm_access").asMap().get("roles"));

            HashMap<String, Integer> hashMap = new HashMap();
            for (String str : roles) {
                hashMap.put(str, str.length());
            }

            return OperationResult.ok(hashMap);
        } catch(Exception e) {
            return OperationResult.fail(e.getMessage());
        }
    }
}

