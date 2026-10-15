package com.domus.tcc.backend.security;

import org.springframework.http.ResponseCookie;

public final class JwtCookieUtil {

    private JwtCookieUtil() {
    }

    public static ResponseCookie criar(String tokenJwt) {
        return ResponseCookie.from("jwtToken", tokenJwt)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(2 * 60 * 60)
                .sameSite("Strict")
                .build();
    }

    public static ResponseCookie limpar() {
        return ResponseCookie.from("jwtToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }
}
