package com.springneobank.auth.messaging;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisteredEvent implements Serializable {
    private Long userId;
    private UUID keycloakId;
    private String email;
    private String name;
    private String lastName;
}
