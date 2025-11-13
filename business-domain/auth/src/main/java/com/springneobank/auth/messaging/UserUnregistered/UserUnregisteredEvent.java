package com.springneobank.auth.messaging.UserUnregistered;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserUnregisteredEvent {
    private Long userId;
}