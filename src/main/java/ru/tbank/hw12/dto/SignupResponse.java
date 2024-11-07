package ru.tbank.hw12.dto;

import lombok.*;
import ru.tbank.hw12.entity.enums.RoleEnum;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupResponse {
    private Long userId;
    private String token;
    private long expiresIn;
    private List<RoleEnum> roles;
}
