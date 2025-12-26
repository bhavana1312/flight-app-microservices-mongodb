package com.flightapp.notificationservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetMessage {

    private String email;
    private String resetLink;
}
