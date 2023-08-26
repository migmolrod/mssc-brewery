package guru.sfg.msscbeerservice.services.inventory;

import guru.sfg.msscbeerservice.services.inventory.model.BeerInventoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Profile("local-discovery")
@Slf4j
@RequiredArgsConstructor
@Service
public class InventoryServiceFeignImpl implements InventoryService {

  private final InventoryServiceFeignClient inventoryServiceFeignClient;

  @Override
  public Integer getOnHandInventory(UUID beerId) {
    log.debug("GOHI - Calling 'Inventory Service' from 'Beer Service' with OpenFeign");

    ResponseEntity<List<BeerInventoryDto>> responseEntity = inventoryServiceFeignClient.getOnHandInventory(beerId);

    List<BeerInventoryDto> result = Objects.requireNonNull(responseEntity.getBody());
    Integer totalInventory = result.stream().mapToInt(BeerInventoryDto::getQuantityOnHand).sum();
    log.debug("GOHI - 'Inventory Service' return {} stock for beer {}", totalInventory, beerId);

    return totalInventory;
  }

}
