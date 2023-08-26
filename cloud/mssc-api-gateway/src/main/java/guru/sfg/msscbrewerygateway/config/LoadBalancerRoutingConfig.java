package guru.sfg.msscbrewerygateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local-discovery")
public class LoadBalancerRoutingConfig {

  @Bean
  public RouteLocator loadBalancerRouting(RouteLocatorBuilder routeLocatorBuilder) {
    return routeLocatorBuilder.routes()

        .route("inventory-service", r -> r
            .path("/api/v1/beer/*/inventory")
            .uri("lb://inventory-service"))

        .route("beer-service", r -> r
            .path("/api/v1/beer/**")
            .uri("lb://beer-service"))

        .route("order-service", r -> r
            .path("/api/v1/customers/**")
            .uri("lb://order-service"))

        .build();
  }
}