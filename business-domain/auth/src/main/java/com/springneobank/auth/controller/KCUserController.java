package com.springneobank.auth.controller;

import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.springneobank.auth.common.OperationResult;
import com.springneobank.auth.common.Utils;
import com.springneobank.auth.entities.KCUser;
import com.springneobank.auth.service.AuthService;
import com.springneobank.auth.service.JwtService;
import com.springneobank.auth.service.KeycloakService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/user")
@Tag(name = "KC User API")
public class KCUserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private KeycloakService kcService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/unregister")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> unregister() {

        OperationResult <?> result = authService.unregisterUser(request);

        if(!result.isSuccess()) {
            return ResponseEntity.internalServerError().body(Map.of("message", result.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", result.getData()));

    }

    @GetMapping("/roles")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> getRoles() throws Exception {

       OperationResult <?> result = jwtService.getRoles(Utils.getAuthorizationHeader(request));

        if(!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", result.getMessage()));
        }

        return ResponseEntity.ok(result.getData());
    }

    @GetMapping("/get_kc_data")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> getData() {
        // Get KC Data
        Map<String, Object> data = kcService.getUserData(KeycloakService.getTokenByAuthHeader(Utils.getAuthorizationHeader(request)));

        // Get User By KCID
        KCUser kcUser = authService.getUserByKCID(UUID.fromString(data.get("sub").toString()));

        // Insert user id
        data.put("user_id", kcUser.getId());

        return ResponseEntity.ok(data);
    }

    @PostMapping("/update_kc_data")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> updateData(@RequestParam("name") String name,
                                        @RequestParam("lastName") String lastName,
                                        @RequestParam("email") String email) {

        OperationResult<?> response = kcService.updateUser(KeycloakService.getTokenByAuthHeader(Utils.getAuthorizationHeader(request)), name, lastName, email);

        if(!response.isSuccess()) {
            return ResponseEntity.internalServerError().body(Map.of("message", response.getMessage()));
        } else {
            return ResponseEntity.ok(Map.of("message", response.getData()));
        }
    }

    @PostMapping("/change_kc_password")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> changeKCPassword(@RequestParam("password") String password) {

        OperationResult<?> response = kcService.changePassword(KeycloakService.getTokenByAuthHeader(Utils.getAuthorizationHeader(request)), password);

        if(!response.isSuccess()) {
            return ResponseEntity.internalServerError().body(Map.of("message", response.getMessage()));
        } else {
            return ResponseEntity.ok(Map.of("message", response.getData()));
        }
    }

    @GetMapping("/get_id_by_authorization")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> getIDByToken() {
        // Get Keycloak ID in auth header
        UUID kcID = KeycloakService.getKeycloakIDByAuthorizationHeader(Utils.getAuthorizationHeader(request));

        KCUser kcUser = authService.getUserByKCID(kcID);

        if(kcUser != null) {
            return ResponseEntity.ok(kcUser.getId());
        }else {
            return ResponseEntity.notFound().build();
        }
    }
}
