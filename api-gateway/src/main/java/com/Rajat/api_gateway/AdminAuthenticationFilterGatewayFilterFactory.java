package com.Rajat.api_gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.http.server.reactive.ServerHttpRequest;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AdminAuthenticationFilterGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AdminAuthenticationFilterGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthenticationFilterGatewayFilterFactory.class);

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AdminAuthenticationFilterGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {

                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("missing authorization header");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    Claims claims = jwtUtil.validateAdminTokenAndGetClaims(authHeader);
                    String username = claims.getSubject();
                    
                    log.info("Request authenticated for user: {}", username);
                    
                    // Mutate request to add X-Auth-User-Id header
                    ServerHttpRequest request = exchange.getRequest().mutate()
                            .header("X-Auth-User-Id", username)
                            .build();
                            
                    return chain.filter(exchange.mutate().request(request).build());

                } catch (Exception e) {
                    log.error("Invalid access token: {}", e.getMessage());

                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {
    }
}
