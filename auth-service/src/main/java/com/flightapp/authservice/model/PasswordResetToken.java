package com.flightapp.authservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection="password_reset_tokens")
public class PasswordResetToken {

    @Id
    private String id;

    private String token;
    private String userId;
    private LocalDateTime expiryTime;
}
