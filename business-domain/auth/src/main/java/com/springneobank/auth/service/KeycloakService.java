/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.springneobank.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springneobank.auth.messaging.UserRegisteredPublisher;
import com.springneobank.auth.repositories.KCUsersRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KeycloakService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${keycloak.user-info-uri}")
    private String keycloakUserInfo;

    @Value("${keycloak.users-uri}")
    private String usersUri;

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
    public UUID registerUser(String username, String password, String email, String name, String lastName, String phone) {

        UUID keycloakID = null;

        // Get superAdmin token
        String token = getAdminAccessToken();

        // Set header with superAdmin token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        // Create payload
        Map<String, Object> payload = Map.of(
            "username", username,
            "email", email,
            "firstName", name,
            "lastName", lastName,
            "enabled", true,
            "emailVerified", true,
            "credentials", List.of(Map.of(
                "type", "password",
                "value", password,
                "temporary", false
            ))
        );

        // Build request
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        // Send request to Keycloak Users URL 
        ResponseEntity<Void> resp = restTemplate.postForEntity(usersUri, request, Void.class);

        if (resp.getStatusCode() == HttpStatus.CREATED) {
            URI location = resp.getHeaders().getLocation();
            if (location != null) {
                // Get keycloak ID
                String path = location.getPath(); // /admin/realms/SpringNeoBank/users/{id}
                keycloakID = UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
            }
        }

        return keycloakID;
    }

    /**
     * Delete a user in Keycloack
     * 
     * @param kcID
     * @return true or false
     */
    public boolean deleteUser(UUID kcID) {
        // Get superAdmin token
        String token = getAdminAccessToken();

        // Set token in headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);        

        // Adding ID to user URI
        String deleteUri = String.format("%s/%s", usersUri, kcID);

        ResponseEntity<Void> response = restTemplate.exchange(
                deleteUri,
                org.springframework.http.HttpMethod.DELETE, // DELETE METHOD
                request,
                Void.class
        );

        return (response.getStatusCode() == HttpStatus.NO_CONTENT) ? true : false;
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

