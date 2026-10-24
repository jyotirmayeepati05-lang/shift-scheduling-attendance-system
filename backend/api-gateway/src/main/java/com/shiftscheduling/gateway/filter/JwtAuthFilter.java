package com.shiftscheduling.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final SecretKey signingKey;

    private final List<String> publicPaths = List.of(
            "/api/auth/login", "/api/auth/health",
            "/v3/api-docs", "/swagger-ui",
            "/eureka"
    );

    public JwtAuthFilter(@Value("${jwt.secret:shift-scheduling-dev-secret-key-change-in-production-2024}") String secret) {
        String padded = secret;
        while (padded.getBytes(StandardCharsets.UTF_8).length < 64) {
            padded = padded + secret;
        }
        this.signingKey = Keys.hmacShaKeyFor(padded.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Allow public paths
        for (String pub : publicPaths) {
            if (path.startsWith(pub)) {
                return chain.filter(exchange);
            }
        }

        // Check Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String role = String.valueOf(claims.get("role"));

            // Attendance overview report: ADMIN and HR only (enforced here from the verified token)
            if (path.startsWith("/api/attendance/overview")
                    && !("ADMIN".equalsIgnoreCase(role) || "HR".equalsIgnoreCase(role))) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // Forward user info as headers to downstream services.
            // Any X-User-* headers sent by the client are removed first so they can never be spoofed.
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .headers(h -> {
                        h.remove("X-User-Id");
                        h.remove("X-User-Role");
                        h.remove("X-Username");
                        h.add("X-User-Id", String.valueOf(claims.get("userId")));
                        h.add("X-User-Role", role);
                        h.add("X-Username", claims.getSubject());
                    })
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
