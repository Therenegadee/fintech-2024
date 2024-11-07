package ru.tbank.hw12.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    private String username;
    private String newPassword;
    private String confirmationCode;
}
