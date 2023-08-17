package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.inventory.service.domain.BeerInventory;
import guru.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import guru.sfg.brewery.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeallocationServiceImpl implements DeallocationService {

  private final BeerInventoryRepository beerInventoryRepository;

  @Override
  public void deallocateOrder(BeerOrderDto beerOrderDto) {
    beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
      BeerInventory beerInventory = BeerInventory.builder()
          .beerId(beerOrderLineDto.getBeerId())
          .upc(beerOrderLineDto.getUpc())
          .quantityOnHand(beerOrderLineDto.getQuantityAllocated())
          .build();

      BeerInventory savedInventory = beerInventoryRepository.save(beerInventory);

      log.debug("Deallocated inventory for beer with UPC {}. Inventory id {}", savedInventory, savedInventory.getId());
    });
  }

}
