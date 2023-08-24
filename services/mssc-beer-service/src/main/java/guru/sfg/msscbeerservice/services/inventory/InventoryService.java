package guru.sfg.msscbeerservice.services.inventory;

import java.util.UUID;

public interface InventoryService {
  Integer getOnHandInventory(UUID beerId);
}
