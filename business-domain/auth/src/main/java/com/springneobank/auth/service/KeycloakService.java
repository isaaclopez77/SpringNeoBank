/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.springneobank.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.springneobank.auth.common.OperationResult;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class KeycloakService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${keycloak.user-info-uri}")
    private String keycloakUserInfo;

    @Value("${keycloak.users-uri}")
    private String usersUri;

    @Value("${keycloak.admin-token-uri}")
    private String adminTokenUri;

    @Value("${keycloak.change-password-uri}")
    private String changePasswordUri;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;


    /**
     * Get Keycloak user data by token
     * 
     * @param token
     * @return
     */
    public Map<String, Object> getUserData(String token){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(keycloakUserInfo, HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }

    /**
     * Register new user
     * 
     * @param username
     * @param email
     * @param password
     */
    public UUID registerUser(String username, String password, String email, String name, String lastName) {

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
     * Deactivate: Set status false in Keycloak Server
     * 
     * @param keycloakID
     */
    public void deactivateUser(UUID keycloakID){
        // Get superAdmin token
        String token = getAdminAccessToken();

        // Set header with superAdmin token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        // Create payload
        Map<String, Object> payload = Map.of("enabled", false);

        // Adding ID to user URI
        String concreteUserUri = String.format("%s/%s", usersUri, keycloakID);

        // Build request
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        // Send request to Keycloak Users URL 
        ResponseEntity<Void> response = restTemplate.exchange(concreteUserUri, org.springframework.http.HttpMethod.PUT, request, Void.class);
    }

    /**
     * Activate: Set status true in Keycloak Server
     * 
     * @param keycloakID
     */
    public void activateUser(UUID keycloakID){
        // Get superAdmin token
        String token = getAdminAccessToken();

        // Set header with superAdmin token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        // Create payload
        Map<String, Object> payload = Map.of("enabled", true);

        // Adding ID to user URI
        String concreteUserUri = String.format("%s/%s", usersUri, keycloakID);

        // Build request
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        // Send request to Keycloak Users URL 
        ResponseEntity<Void> response = restTemplate.exchange(concreteUserUri, org.springframework.http.HttpMethod.PUT, request, Void.class);
    }

    /**
     * Update user data in Keycloak
     * 
     * @param token
     * @param name
     * @param lastName
     * @param email
     * @return
     */
    public OperationResult<String> updateUser(String token, String name, String lastName, String email){
        try{
            // Get superAdmin token
            String adminToken = getAdminAccessToken();

            // Set Authorization header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);

            // Adding ID to user URI
            String concreteUserUri = String.format("%s/%s", usersUri, getKeycloakIDByToken(token));

            // Build payload
            Map<String, Object> payload = Map.of(
                                        "firstName", name,
                                        "lastName", lastName,
                                        "email", email
                                        );
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            // Do request
            ResponseEntity<Void> response = restTemplate.exchange(concreteUserUri, HttpMethod.PUT, entity, Void.class);

            return OperationResult.ok("KC User updated");
        } catch(Exception e) {
            return OperationResult.fail(e.getMessage());
        }
    }

    public OperationResult<String> changePassword(String token, String password){
        try{
            // Get superAdmin token
            String adminToken = getAdminAccessToken();

            // Set Authorization header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);

            // Adding ID to change password URI
            String changePasswordURIBuilded = changePasswordUri.replace("*user*", getKeycloakIDByToken(token).toString());

            // Build payload
            Map<String, Object> payload = Map.of(
                                        "type", "password",
                                        "value", password,
                                        "temporary", false
                                        );
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            // Do request
            ResponseEntity<Void> response = restTemplate.exchange(changePasswordURIBuilded, HttpMethod.PUT, entity, Void.class);

            return OperationResult.ok("KC User updated");
        } catch(Exception e) {
            return OperationResult.fail(e.getMessage());
        }
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
     * GET Keycloak UUID inside the Authorization header
     * 
     * @param token
     * @return
     */
    public static UUID getKeycloakIDByAuthorizationHeader(String authHeader){
        String token = getTokenByAuthHeader(authHeader);
        return getKeycloakIDByToken(token);
    }

    /**
     * Extract token inside the auth header
     * 
     * @param authHeader
     * @return
     */
    public static String getTokenByAuthHeader(String authHeader){
        return authHeader.replace("Bearer", "").trim();
    }

    /**
     * Extract KCID in token
     * 
     * @param token
     * @return
     */
    public static UUID getKeycloakIDByToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return UUID.fromString(jwt.getSubject());
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

