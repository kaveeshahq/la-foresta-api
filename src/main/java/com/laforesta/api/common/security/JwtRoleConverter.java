package com.laforesta.api.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class JwtRoleConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        List<String> roles =
                jwt.getClaimAsStringList("roles");

        Collection<GrantedAuthority> authorities;

        if (roles == null) {

            authorities = Collections.emptyList();

        } else {

            authorities = roles.stream()
                    .map(role ->
                            (GrantedAuthority)
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + role
                                    )
                    )
                    .toList();
        }

        return new JwtAuthenticationToken(
                jwt,
                authorities,
                jwt.getSubject()
        );
    }
}