package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.inventory.service.config.JmsConfig;
import guru.sfg.beer.inventory.service.domain.BeerInventory;
import guru.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import guru.sfg.brewery.model.BeerDto;
import guru.sfg.brewery.model.events.AddInventoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddInventoryListener {

  private final BeerInventoryRepository beerInventoryRepository;

  @JmsListener(destination = JmsConfig.ADD_INVENTORY_QUEUE)
  public void listen(AddInventoryEvent event) {
    BeerDto beerDto = event.getBeerDto();

    log.debug("'Add inventory' event caught for beer {} ({}). Brewing {}",
        beerDto.getBeerName(), beerDto.getUpc(), beerDto.getQuantityToBrew());

    BeerInventory beerInventory = beerInventoryRepository.findOneByUpc(beerDto.getUpc());

    if (beerInventory == null) {
      beerInventory = BeerInventory
          .builder()
          .beerId(beerDto.getId())
          .upc(beerDto.getUpc())
          .quantityOnHand(beerDto.getQuantityOnHand())
          .build();
    } else {
      beerInventory.setQuantityOnHand(beerInventory.getQuantityOnHand() + beerDto.getQuantityToBrew());
    }

    beerInventoryRepository.save(beerInventory);
  }
}
