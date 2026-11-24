package com.filacero.security;

import com.filacero.model.Rol;
import com.filacero.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/** Genera y valida los tokens JWT (HS256). La clave sale de la variable de entorno JWT_SECRET. */
@Service
public class JwtServicio {

    private final SecretKey clave;
    private final long horasValidez;

    public JwtServicio(@Value("${filacero.jwt.secreto}") String secreto,
                       @Value("${filacero.jwt.horas-validez:12}") long horasValidez) {
        if (secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.horasValidez = horasValidez;
    }

    public String generar(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getId())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + horasValidez * 3_600_000))
                .signWith(clave)
                .compact();
    }

    public Optional<UsuarioActual> validar(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return Optional.of(new UsuarioActual(claims.getSubject(), claims.get("nombre", String.class),
                    Rol.valueOf(claims.get("rol", String.class))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
