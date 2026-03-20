package com.revature.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Key;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    // Default secret for development purposes. Should be externalized in vault or config server.
    @Value("${jwt.secret:defaultSecretKeyWithAtLeast256BitsForSecurityP3Project12345!}")
    private String secretKey;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Skip validation for public auth and forgot-password endpoints
            String path = request.getURI().getPath();


            if (path.startsWith("/api/auth/") ||
                    path.startsWith("/api/forgot/") ||
                    path.startsWith("/api/password/") ||
                    path.startsWith("/api/otp/")) {
                System.out.println("GATEWAY FORWARDING: " + exchange.getRequest().getURI());
                System.out.println("Skipping JWT filter for: " + path);
                return chain.filter(exchange);
            }
            System.out.println("JWT FILTER RUNNING FOR: " + path);
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (!authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid auth header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            System.out.println("GATEWAY FORWARDING: " + exchange.getRequest().getURI());
            try {
                byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey);
                Key key = Keys.hmacShaKeyFor(keyBytes);
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // Mutate request to pass username downstream
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-Logged-In-Username", claims.getSubject())
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                return onError(exchange, "JWT validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(org.springframework.web.server.ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        // Add log here in a real app
        return response.setComplete();
    }

    public static class Config {
        // configuration properties
    }
}
