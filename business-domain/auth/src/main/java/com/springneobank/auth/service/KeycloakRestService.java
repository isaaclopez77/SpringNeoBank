/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.springneobank.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakRestService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${keycloak.user-info-uri}")
    private String keycloakUserInfo;

    @Value("${keycloak.create-user-uri}")
    private String createUserUri;

    @Value("${keycloak.admin-token-uri}")
    private String adminTokenUri;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    /**
     * Check if a token is valid 
     * A successful user token will generate http code 200, other than that will create an exception
     *
     * @param token
     * @return
     * @throws Exception
     */
    public String checkValidity(String token) throws Exception {
        return getUserInfo(token);
    }

    /**
     * Get rolesby token
     * 
     * @param token
     * @return
     * @throws Exception
     */
    public List<String> getRoles(String token) throws Exception {
        String response = getUserInfo(token);

        // get roles
        Map map = new ObjectMapper().readValue(response, HashMap.class);
        return (List<String>) map.get("roles");
    }


    /**
     * Get user info by token
     * 
     * @param token
     * @return
     */
    private String getUserInfo(String token) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();     
        headers.add("Authorization", token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        return restTemplate.postForObject(keycloakUserInfo, request, String.class);
    }

    /**
     * Register new user
     * 
     * @param username
     * @param email
     * @param password
     */
    public void registerUser(String username, String email, String password) {
        String token = getAdminAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> payload = Map.of(
            "username", username,
            "email", email,
            "enabled", true,
            "credentials", List.of(Map.of(
                "type", "password",
                "value", password,
                "temporary", false
            ))
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        restTemplate.postForEntity(createUserUri, request, Void.class);
    }

    /**
     * Get Admin token
     * 
     * @return
     */
    private String getAdminAccessToken() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);

        return restTemplate.postForObject(adminTokenUri, params, Map.class).get("access_token").toString();
    }

}

