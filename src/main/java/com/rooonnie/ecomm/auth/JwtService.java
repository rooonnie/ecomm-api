package com.rooonnie.ecomm.auth;

import com.rooonnie.ecomm.customer.Customer;
import com.rooonnie.ecomm.customer.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${ecomm.jwt.secret}") String secret,
            @Value("${ecomm.jwt.expiration-ms}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String issue(Customer customer) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(customer.getId()))
                .claim("email", customer.getEmail())
                .claim("role", customer.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    public AuthPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String roleName = claims.get("role", String.class);
        UserRole role = roleName == null ? UserRole.CUSTOMER : UserRole.valueOf(roleName);
        return new AuthPrincipal(Long.parseLong(claims.getSubject()), claims.get("email", String.class), role);
    }
}
