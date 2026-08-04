package com.stockpro.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtAuthGatewayFilter implements GatewayFilter {

    // These paths bypass the JWT check entirely
    private static final List<String> OPEN_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/validate",
            "/api/v1/auth/logout",
            "/actuator"
    );

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Let open paths through without any token
        boolean isOpen = OPEN_PATHS.stream().anyMatch(path::startsWith);
        if (isOpen) {
            return chain.filter(exchange);
        }

        // Check Authorization header
        String authHeader = exchange.getRequest()
                .getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Request to {} rejected — missing Authorization header", path);
            return writeError(exchange,
                    HttpStatus.UNAUTHORIZED,
                    "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            // Parse and validate JWT locally — same as your service JwtAuthFilters
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Double-check expiry (JJWT throws ExpiredJwtException but belt-and-suspenders)
            if (claims.getExpiration().before(new Date())) {
                return writeError(exchange, HttpStatus.UNAUTHORIZED, "Token has expired");
            }

            // Extract claims
            String email  = claims.getSubject();
            String role   = claims.get("role", String.class);
            String userId = String.valueOf(claims.get("userId"));

            log.debug("Gateway auth OK — email={}, role={}, path={}", email, role, path);

            // Forward original token + enriched headers to downstream service
            // Downstream JwtAuthFilter will re-validate — this is intentional double-check
            ServerWebExchange enriched = exchange.mutate()
                    .request(req -> req.headers(headers -> {
                        headers.set(HttpHeaders.AUTHORIZATION, authHeader); // keep original
                        headers.set("X-Auth-Email",  email);
                        headers.set("X-Auth-Role",   role);
                        headers.set("X-Auth-UserId", userId);
                    }))
                    .build();

            return chain.filter(enriched);

        } catch (ExpiredJwtException e) {
            log.warn("Gateway rejected expired token for path={}", path);
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Token has expired");

        } catch (SignatureException e) {
            log.warn("Gateway rejected invalid signature for path={}", path);
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Invalid token signature");

        } catch (MalformedJwtException e) {
            log.warn("Gateway rejected malformed token for path={}", path);
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Malformed token");

        } catch (Exception e) {
            log.error("Gateway token validation error for path={}: {}", path, e.getMessage());
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Token validation failed");
        }
    }

    private Mono<Void> writeError(ServerWebExchange exchange,
                                  HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"error\":\"%s\",\"message\":\"%s\",\"status\":%d}",
                status.name(), message, status.value()
        );

        var buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes());

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}