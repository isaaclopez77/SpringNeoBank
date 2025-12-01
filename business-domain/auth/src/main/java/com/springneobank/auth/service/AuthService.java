package com.springneobank.auth.service;

import java.security.interfaces.RSAPublicKey;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.auth0.jwk.Jwk;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.springneobank.auth.common.OperationResult;
import com.springneobank.auth.common.Utils;
import com.springneobank.auth.entities.KCUser;
import com.springneobank.auth.messaging.UserRegistered.UserRegisteredEvent;
import com.springneobank.auth.messaging.UserRegistered.UserRegisteredPublisher;
import com.springneobank.auth.messaging.UserUnregistered.UserUnregisteredEvent;
import com.springneobank.auth.messaging.UserUnregistered.UserUnregisteredPublisher;
import com.springneobank.auth.repositories.KCUsersRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthService {

    @Autowired
    private KCUsersRepository uRepository;

    @Autowired
    private KeycloakService kcService;

    @Autowired
    private UserRegisteredPublisher urEventPublisher;

    @Autowired
    private UserUnregisteredPublisher unrEventPublisher;

    /**
     * Register User in application
     * - Create in keycloak
     * - Create in DDBB
     * - Publish RabbitMQ event
     * 
     * @param username
     * @param email
     * @param name
     * @param lastName
     * @param password
     * @param phone
     * @return
     */
    public OperationResult<String> registerUser(String username, String email, String name, String lastName, String password, String phone) {

        try{
            // Create user in keycloack 
            UUID keycloakID = kcService.registerUser(username, password, email, name, lastName);

            if(keycloakID != null) {
                // Create database relation
                KCUser kcUser = uRepository.save(new KCUser(keycloakID, true));
                    
                // Register RabbitMQ event
                UserRegisteredEvent event = UserRegisteredEvent.builder()
                    .userId(kcUser.getId())
                    .phone(phone)
                    .build();

                urEventPublisher.publish(event);

                return OperationResult.ok("User registered");
            } else {
                return OperationResult.fail("Error while registering in Keycloak");
            }
        }catch(Exception e) {
            return OperationResult.fail(e.getMessage());
        }
    }

    /**
     * Unregister user in application
     * - Disable user in keycloak
     * - Disable in DDBB
     * - Publish RabbitMQ event
     * 
     * @param request
     * @return
     */
    public OperationResult<String> unregisterUser(HttpServletRequest request) {

        try{
            // Get Keycloak ID in auth header
            UUID kcID = KeycloakService.getKeycloakIDByAuthorizationHeader(Utils.getAuthorizationHeader(request));

            KCUser kcUser = getUserByKCID(kcID);

            if(kcUser != null) {
                // Deactivate in keycloak
                kcService.deactivateUser(kcID);

                // In database
                deactivateUser(kcUser);
                
                // Unregister Rabbit event
                UserUnregisteredEvent event = UserUnregisteredEvent.builder()
                    .userId(kcUser.getId())
                    .build();

                unrEventPublisher.publish(event);

                return OperationResult.ok("User disabled");
            } else {
                return OperationResult.fail("User not found");
            }
        }catch(Exception e) {
            return OperationResult.fail(e.getMessage());
        }
    }

    /**
     * Deactivate: Set user status 0 in Database
     * 
     * @param kcuser
     * @return
     */
    public KCUser deactivateUser(KCUser kcuser) {
        kcuser.setStatus(false);
        return uRepository.save(kcuser);
    }

    /**
     * Activate: Set user status 1 in Database
     * 
     * @param kcuser
     * @return
     */
    public KCUser activateUser(KCUser kcuser) {
        kcuser.setStatus(true);
        return uRepository.save(kcuser);
    }

    /**
     * Remove user by Keycloack ID
     * 
     * @param keycloakID
     */
    public void removeUser(KCUser user) {
        uRepository.delete(user);
    }

    /**
     * Get KC UUID by ID
     * 
     * @param id
     * @return
     */
    public KCUser getUserByID(Long id) {
        return uRepository.findById(id).orElse(null);
    }

    /**
     * Get KCUser by KCID
     * 
     * @param id
     * @return
     */
    public KCUser getUserByKCID(UUID id) {
        return uRepository.findByKeycloakID(id).orElse(null);
    }

    /**
     * Get Keycloak ID by user ID
     * 
     * @param id
     * @return
     */
    public UUID getKeycloakIDByUserID(Long id) {
        UUID kcID = null;

        Optional<KCUser> optUser = uRepository.findById(id);
        if(optUser.isPresent()) {
            kcID = optUser.get().getKeycloakID();
        }

        return kcID;
    }

}
