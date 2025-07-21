package com.financeMonkey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenResponseDto {
    private String token;
    private String refreshToken;
    private UUID userId;
    private String email;
}
