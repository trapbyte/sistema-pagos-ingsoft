package com.umanizales.pagos.config.security;

import com.umanizales.pagos.cliente.entity.RolUsuario;
import com.umanizales.pagos.common.security.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    private static final String ROL_CLAIM = "rol";

    private final SecretKey signingKey;
    private final long expirationMinutes;

    public JwtService(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes());
        this.expirationMinutes = properties.expirationMinutes();
    }

    public GeneratedToken generarToken(UUID clienteId, RolUsuario rol) {
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        String token = Jwts.builder()
            .subject(clienteId.toString())
            .claim(ROL_CLAIM, rol.name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(signingKey)
            .compact();
        return new GeneratedToken(token, expiration);
    }

    public record GeneratedToken(String token, Instant expiresAt) {
    }

    public Optional<AuthenticatedUser> validarYExtraer(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            UUID clienteId = UUID.fromString(claims.getSubject());
            RolUsuario rol = RolUsuario.valueOf(claims.get(ROL_CLAIM, String.class));
            return Optional.of(new AuthenticatedUser(clienteId, rol));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
