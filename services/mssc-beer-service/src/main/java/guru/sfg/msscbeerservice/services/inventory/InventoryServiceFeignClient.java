package guru.sfg.msscbeerservice.services.inventory;

import guru.sfg.msscbeerservice.services.inventory.model.BeerInventoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "inventory-service")
public interface InventoryServiceFeignClient {

  @GetMapping(value = InventoryServiceRestTemplateImpl.INVENTORY_URI)
  ResponseEntity<List<BeerInventoryDto>> getOnHandInventory(@PathVariable UUID beerId);

}
