package com.laforesta.api.user.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;

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

    @GetMapping("/authorities")
    public Map<String, Object> authorities(
            Authentication authentication
    ) {

        return Map.of(
                "username",
                authentication.getName(),

                "authorities",
                authentication.getAuthorities()
                        .stream()
                        .map(Object::toString)
                        .toList()
        );
    }


}