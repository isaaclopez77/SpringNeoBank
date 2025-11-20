package com.springneobank.auth.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.springneobank.auth.common.OperationResult;
import com.springneobank.auth.entities.KCUser;
import com.springneobank.auth.messaging.UserUnregistered.UserUnregisteredEvent;
import com.springneobank.auth.messaging.UserUnregistered.UserUnregisteredPublisher;
import com.springneobank.auth.service.KCUserService;
import com.springneobank.auth.service.KeycloakService;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/user")
public class KCUserController {

    @Autowired
    private UserUnregisteredPublisher unrEventPublisher;

    @Autowired
    private KCUserService databaseService;

    @Autowired
    private KeycloakService kcService;

    @PostMapping("/unregister")
    public ResponseEntity<?> unregister(@RequestHeader("Authorization") String authHeader) {

        // Get Keycloak ID in auth header
        UUID kcID = KeycloakService.getKeycloakIDByAuthorizationHeader(authHeader);

        KCUser kcUser = databaseService.getUserByKCID(kcID);

        if(kcUser != null) {
            // Deactivate in keycloak
            kcService.deactivateUser(kcID);

            // In database
            databaseService.deactivateUser(kcUser);
            
            // Unregister Rabbit event
            UserUnregisteredEvent event = UserUnregisteredEvent.builder()
                .userId(kcUser.getId())
                .build();

            unrEventPublisher.publish(event);
        } else {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.status(HttpStatus.OK).build();

    }

    @GetMapping("/roles")
    public ResponseEntity<?> getRoles(@RequestHeader("Authorization") String authHeader) throws Exception {
        DecodedJWT jwt = JWT.decode(authHeader.replace("Bearer", "").trim());

        // Check JWT role is correct
        List<String> roles = ((List) jwt.getClaim("realm_access").asMap().get("roles"));

        HashMap HashMap = new HashMap();
        for (String str : roles) {
            HashMap.put(str, str.length());
        }
        return ResponseEntity.ok(HashMap);
    }

    @GetMapping("/get_kc_data")
    public ResponseEntity<?> getData(@RequestHeader("Authorization") String authHeader) {
        // Get KC Data
        Map<String, Object> data = kcService.getUserData(KeycloakService.getTokenByAuthHeader(authHeader));

        // Get User By KCID
        KCUser kcUser = databaseService.getUserByKCID(UUID.fromString(data.get("sub").toString()));

        // Insert user id
        data.put("user_id", kcUser.getId());

        return ResponseEntity.ok(data);
    }

    @PostMapping("/update_kc_data")
    public ResponseEntity<?> updateData(@RequestHeader("Authorization") String authHeader,
                                        @RequestParam("name") String name,
                                        @RequestParam("lastName") String lastName,
                                        @RequestParam("email") String email) {

        OperationResult<?> response = kcService.updateUser(KeycloakService.getTokenByAuthHeader(authHeader), name, lastName, email);

        if(!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", response.getMessage()));
        } else {
            return ResponseEntity.ok(Map.of("message", response.getData()));
        }
    }

    @PostMapping("/change_kc_password")
    public ResponseEntity<?> changeKCPassword(@RequestHeader("Authorization") String authHeader, @RequestParam("password") String password) {

        OperationResult<?> response = kcService.changePassword(KeycloakService.getTokenByAuthHeader(authHeader), password);

        if(!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", response.getMessage()));
        } else {
            return ResponseEntity.ok(Map.of("message", response.getData()));
        }
    }

    @GetMapping("/get_id_by_authorization")
    public ResponseEntity<?> getIDByToken(@RequestHeader("Authorization") String authHeader) {
        // Get Keycloak ID in auth header
        UUID kcID = KeycloakService.getKeycloakIDByAuthorizationHeader(authHeader);

        KCUser kcUser = databaseService.getUserByKCID(kcID);

        if(kcUser != null) {
            return ResponseEntity.ok(kcUser.getId());
        }else {
            return ResponseEntity.notFound().build();
        }
    }
    
    
}
