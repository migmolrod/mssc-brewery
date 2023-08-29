package guru.sfg.msscbeerservice.services.inventory;

import guru.sfg.msscbeerservice.services.inventory.model.BeerInventoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "inventory-failover")
public interface InventoryFailoverFeignClient {

  @GetMapping(value = InventoryServiceRestTemplateImpl.INVENTORY_FAILOVER_URI)
  ResponseEntity<List<BeerInventoryDto>> getOnHandInventory();

}
