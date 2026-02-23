package com.springboot.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseEntity{
    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    @NotBlank
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    @Email
    @NotBlank
    private String email;

    @Column(name = "password_hash", nullable = false)
    @NotBlank
    private String passwordHash;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}
