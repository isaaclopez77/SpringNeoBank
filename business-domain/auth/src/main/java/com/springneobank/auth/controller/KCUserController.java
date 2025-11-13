package com.springneobank.auth.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.springneobank.auth.entities.KCUser;
import com.springneobank.auth.messaging.UserUnregistered.UserUnregisteredEvent;
import com.springneobank.auth.messaging.UserUnregistered.UserUnregisteredPublisher;
import com.springneobank.auth.service.KCUserService;
import com.springneobank.auth.service.KeycloakService;

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
    public ResponseEntity<?> unregister(@RequestParam("user_id") Long user_id) {

        KCUser kc_user = databaseService.getUserByID(user_id);

        if(kc_user != null) {
            // Deactivate in keycloak
            kcService.deactivateUser(kc_user.getKeycloakID());

            // In database
            databaseService.deactivateUser(kc_user);
            
            // Unregister Rabbit event
            UserUnregisteredEvent event = UserUnregisteredEvent.builder()
                .userId(kc_user.getId())
                .build();

            unrEventPublisher.publish(event);

            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

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
}
