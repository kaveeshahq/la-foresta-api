package com.laforesta.api.user.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public Map<String, Object> me(
            @AuthenticationPrincipal Jwt jwt
    ) {

        return Map.of(
                "userId", jwt.getSubject(),
                "email", jwt.getClaimAsString("email"),
                "roles", jwt.getClaimAsStringList("roles")
        );
    }
}