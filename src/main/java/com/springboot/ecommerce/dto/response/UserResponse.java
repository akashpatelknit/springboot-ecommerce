package com.springboot.ecommerce.dto.response;

import com.springboot.ecommerce.entity.UserStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserStatus status;
    private Boolean isEmailVerified;
    private LocalDateTime lastLoginAt;
    private Set<String> roles;       // role names e.g. ["ADMIN", "BUYER"]
    private Instant createdAt;
    private Instant updatedAt;
}