package guru.sfg.msscbrewerygateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class LocalhostRoutingConfig {

  @Bean
  public RouteLocator localhostRouting(RouteLocatorBuilder routeLocatorBuilder) {
    return routeLocatorBuilder.routes()

        .route("inventory-service", r -> r
            .path("/api/v1/beer/*/inventory")
            .uri("http://localhost:9033"))

        .route("beer-service", r -> r
            .path("/api/v1/beer/**")
            .uri("http://localhost:9031"))

        .route("order-service", r -> r
            .path("/api/v1/customers/**")
            .uri("http://localhost:9032"))

        .build();
  }

}
