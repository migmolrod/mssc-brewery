package guru.sfg.msscbeerservice.services.inventory;

import guru.sfg.msscbeerservice.services.inventory.model.BeerInventoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class InventoryFailoverFeignClientImpl implements InventoryServiceFeignClient {

  private final InventoryFailoverFeignClient inventoryFailoverFeignClient;

  @Override
  public ResponseEntity<List<BeerInventoryDto>> getOnHandInventory(UUID beerId) {
    log.debug("GOHI - Calling 'Inventory Failover' with OpenFeign instead");

    return inventoryFailoverFeignClient.getOnHandInventory();
  }
}
