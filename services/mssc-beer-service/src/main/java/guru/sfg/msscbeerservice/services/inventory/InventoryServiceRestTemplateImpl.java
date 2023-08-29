package guru.sfg.msscbeerservice.services.inventory;

import guru.sfg.msscbeerservice.services.inventory.model.BeerInventoryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Profile("!local-discovery")
@Slf4j
@ConfigurationProperties(prefix = "sfg.brewery", ignoreUnknownFields = false)
@Component
public class InventoryServiceRestTemplateImpl implements InventoryService {

  // REFACTOR - move this to abstract class with constants or to configuration
  public final static String INVENTORY_SERVICE_URI = "/api/v1/beer/{beerId}/inventory";
  public final static String INVENTORY_FAILOVER_URI = "/api/v1/inventory-failover";
  private final RestTemplate restTemplate;

  private String beerInventoryServiceHost;

  public void setBeerInventoryServiceHost(String beerInventoryServiceHost) {
    this.beerInventoryServiceHost = beerInventoryServiceHost;
  }

  public InventoryServiceRestTemplateImpl(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  @Override
  public Integer getOnHandInventory(UUID beerId) {
    log.debug("GOHI - Calling 'Inventory Service' from 'Beer Service' with RestTemplate");

    ResponseEntity<List<BeerInventoryDto>> responseEntity = restTemplate.exchange(
        beerInventoryServiceHost + INVENTORY_SERVICE_URI,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<>() {
        },
        beerId
    );

    List<BeerInventoryDto> result = Objects.requireNonNull(responseEntity.getBody());
    Integer totalInventory = result.stream().mapToInt(BeerInventoryDto::getQuantityOnHand).sum();
    log.debug("GOHI - 'Inventory Service' return {} stock for beer {}", totalInventory, beerId);

    return totalInventory;
  }
}
