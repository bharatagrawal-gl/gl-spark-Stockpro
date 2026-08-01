package com.stockpro.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayFilterConfig {

    private final JwtAuthGatewayFilter jwtAuthGatewayFilter;

    public GatewayFilterConfig(JwtAuthGatewayFilter jwtAuthGatewayFilter) {
        this.jwtAuthGatewayFilter = jwtAuthGatewayFilter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()

                // ── Auth service — no JWT filter (login/register/validate are open) ──
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .uri("lb://auth-service"))

                // ── All other services — JWT filter applied ──────────────────────────

                .route("product-service", r -> r
                        .path("/api/v1/products/**")
                        .filters(f -> f.filter(jwtAuthGatewayFilter))
                        .uri("lb://product-service"))

                .route("warehouse-service", r -> r
                        .path("/api/v1/warehouses/**")
                        .filters(f -> f.filter(jwtAuthGatewayFilter))
                        .uri("lb://warehouse-service"))

                .route("purchase-service", r -> r
                        .path("/api/v1/purchase-orders/**")
                        .filters(f -> f.filter(jwtAuthGatewayFilter))
                        .uri("lb://purchase-service"))

                .route("supplier-service", r -> r
                        .path("/api/v1/suppliers/**")
                        .filters(f -> f.filter(jwtAuthGatewayFilter))
                        .uri("lb://supplier-service"))

                .route("movement-service", r -> r
                        .path("/api/v1/movements/**")
                        .filters(f -> f.filter(jwtAuthGatewayFilter))
                        .uri("lb://movement-service"))

                .route("alert-service", r -> r
                        .path("/api/v1/alerts/**")
                        .filters(f -> f.filter(jwtAuthGatewayFilter))
                        .uri("lb://alert-service"))

                .route("report-service", r -> r
                        .path("/api/v1/reports/**")
                        .filters(f -> f.filter(jwtAuthGatewayFilter))
                        .uri("lb://report-service"))

                .build();
    }
}