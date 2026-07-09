package com.Rajat.api_gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;

import java.security.Key;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.admin-secret}")
    private String adminSecret;

    public Claims validateTokenAndGetClaims(final String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey(secret)).build().parseClaimsJws(token).getBody();
    }

    public Claims validateAdminTokenAndGetClaims(final String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey(adminSecret)).build().parseClaimsJws(token).getBody();
    }

    public void validateToken(final String token) {
        validateTokenAndGetClaims(token);
    }

    public void validateAdminToken(final String token) {
        validateAdminTokenAndGetClaims(token);
    }

    private Key getSignKey(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
