package guru.sfg.msscbeerservice.services.brewing;

import guru.sfg.brewery.model.BeerDto;
import guru.sfg.brewery.model.events.BeerEvent;
import guru.sfg.brewery.model.events.BrewBeerEvent;
import guru.sfg.msscbeerservice.config.JmsConfig;
import guru.sfg.msscbeerservice.domain.Beer;
import guru.sfg.msscbeerservice.repositories.BeerRepository;
import guru.sfg.msscbeerservice.services.inventory.InventoryService;
import guru.sfg.msscbeerservice.web.mappers.BeerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BrewingService {

  private final BeerRepository beerRepository;
  private final InventoryService inventoryService;
  private final JmsTemplate jmsTemplate;
  private final BeerMapper beerMapper;

  @Scheduled(fixedRate = 30000)
  public void checkForLowInventory() {
    List<Beer> beers = beerRepository.findAll();

    for (Beer beer : beers) {
      Integer inventoryQuantityOnHand = inventoryService.getOnHandInventory(beer.getId());

      if (beer.getMinOnHand() > inventoryQuantityOnHand) {
        log.debug("CFLI - Beer {} ({}) low stock. Min on hand = {} and current inventory = {}. Sending 'brewing " +
            "request' event", beer.getBeerName(), beer.getId(), beer.getMinOnHand(), inventoryQuantityOnHand);

        BeerDto beerDto = beerMapper.beerToBeerDto(beer);
        beerDto.setQuantityToBrew(beer.getMinOnHand() - inventoryQuantityOnHand);
        beerDto.setQuantityOnHand(inventoryQuantityOnHand);

        BeerEvent event = new BrewBeerEvent(beerDto);

        jmsTemplate.convertAndSend(JmsConfig.BREWING_REQUEST_QUEUE, event);
      } else {
        log.debug("CFLI - Beer {} ({}) is good on stock", beer.getBeerName(), beer.getId());
      }
    }
  }
}
