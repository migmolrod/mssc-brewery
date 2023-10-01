package guru.sfg.msscbrewerygateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local-discovery")
public class LoadBalancerRoutingConfig {

  private static final String REWRITE_PATH_REGEX = "/(?<base>.*?)/(?<segment>.*)";
  private static final String REWRITE_PATH_REPLACEMENT_V1 = "/api/v1/${segment}";

  @Bean
  public RouteLocator loadBalancerRouting(RouteLocatorBuilder routeLocatorBuilder) {

    return routeLocatorBuilder.routes()

        // EXAMPLE WITH CIRCUITBREAKER (FAILOVER) */
        /*.route("inventory-service", r -> r
            .path("/inventory-service/**")
            .filters(f -> f
                .rewritePath(REWRITE_PATH_REGEX, REWRITE_PATH_REPLACEMENT_V1)
                .circuitBreaker(config -> config
                    .setName("inventory-circuit-breaker")
                    .setFallbackUri("forward:/inventory-failover")
                    .setRouteId("inventory-fallback")
                )
            )
            .uri("lb://inventory-service"))*/

        .route("inventory-service", r -> r
            .path("/inventory-service/**")
            .filters(f -> f
                .rewritePath(REWRITE_PATH_REGEX, REWRITE_PATH_REPLACEMENT_V1)
            )
            .uri("lb://inventory-service"))

        .route("beer-service", r -> r
            .path("/beer-service/**")
            .filters(f -> f.rewritePath(REWRITE_PATH_REGEX, REWRITE_PATH_REPLACEMENT_V1))
            .uri("lb://beer-service"))

        .route("order-service", r -> r
            .path("/order-service/**")
            .filters(f -> f.rewritePath(REWRITE_PATH_REGEX, REWRITE_PATH_REPLACEMENT_V1))
            .uri("lb://order-service"))

        .route("inventory-failover", r -> r
            .path("/inventory-failover")
            .filters(f -> f.rewritePath(REWRITE_PATH_REGEX, REWRITE_PATH_REPLACEMENT_V1))
            .uri("lb://inventory-failover"))

        .build();
  }
}
