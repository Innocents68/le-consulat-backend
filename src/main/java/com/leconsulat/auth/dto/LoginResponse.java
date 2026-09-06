package com.leconsulat.auth.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        UserDto user
) {
}
