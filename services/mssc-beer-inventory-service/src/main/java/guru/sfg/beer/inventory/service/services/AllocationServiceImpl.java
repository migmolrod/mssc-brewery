package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.inventory.service.domain.BeerInventory;
import guru.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.BeerOrderLineDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationServiceImpl implements AllocationService {

  private final BeerInventoryRepository beerInventoryRepository;

  @Override
  public Boolean allocateOrder(BeerOrderDto beerOrderDto) {
    log.debug("Allocating order id {}", beerOrderDto.getId());

    AtomicInteger totalOrdered = new AtomicInteger();
    AtomicInteger totalAllocated = new AtomicInteger();

    beerOrderDto.getBeerOrderLines().forEach(line -> {
      int orderQuantity;
      int orderQuantityAllocated;
      if (line.getOrderQuantity() != null)
        orderQuantity = line.getOrderQuantity();
      else
        orderQuantity = 0;

      if (line.getQuantityAllocated() != null)
        orderQuantityAllocated = line.getQuantityAllocated();
      else
        orderQuantityAllocated = 0;

      if (orderQuantity - orderQuantityAllocated > 0) {
        allocateBeerOrderLine(line);
      }

      totalOrdered.set(totalOrdered.get() + line.getOrderQuantity());
      totalAllocated.set(totalAllocated.get() + (line.getQuantityAllocated() != null ? line.getQuantityAllocated() : 0));
    });

    return totalOrdered.get() == totalAllocated.get();
  }

  private void allocateBeerOrderLine(BeerOrderLineDto beerOrderLineDto) {
    List<BeerInventory> beerInventoryList = beerInventoryRepository.findAllByUpc(beerOrderLineDto.getUpc());

    beerInventoryList.forEach(beerInventory -> {
      int inventory = (beerInventory.getQuantityOnHand() != null ? beerInventory.getQuantityOnHand() : 0);
      int orderQty = (beerOrderLineDto.getOrderQuantity() != null ? beerOrderLineDto.getOrderQuantity() : 0);
      int allocatedQty = (beerOrderLineDto.getQuantityAllocated() != null ? beerOrderLineDto.getQuantityAllocated() : 0);
      int qtyToAllocate = orderQty - allocatedQty;

      if (inventory >= qtyToAllocate) {
        inventory = inventory - qtyToAllocate;
        beerOrderLineDto.setQuantityAllocated(orderQty);
        beerInventory.setQuantityOnHand(inventory);

        beerInventoryRepository.save(beerInventory);
      } else if (inventory > 0) {
        beerOrderLineDto.setQuantityAllocated(allocatedQty + inventory);
        beerInventory.setQuantityOnHand(0);

        beerInventoryRepository.delete(beerInventory);
      }
    });
  }

}
