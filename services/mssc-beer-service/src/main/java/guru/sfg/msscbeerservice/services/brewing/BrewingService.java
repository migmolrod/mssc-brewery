package guru.sfg.msscbeerservice.services.brewing;

import guru.sfg.msscbeerservice.config.JmsConfig;
import guru.sfg.msscbeerservice.domain.Beer;
import guru.sfg.msscbeerservice.events.BeerEvent;
import guru.sfg.msscbeerservice.events.BrewBeerEvent;
import guru.sfg.msscbeerservice.repositories.BeerRepository;
import guru.sfg.msscbeerservice.services.inventory.BeerInventoryService;
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
  private final BeerInventoryService beerInventoryService;
  private final JmsTemplate jmsTemplate;
  private final BeerMapper beerMapper;

  @Scheduled(fixedRate = 30000)
  public void checkForLowInventory() {
    List<Beer> beers = beerRepository.findAll();

    for (Beer beer : beers) {
      Integer inventoryQuantityOnHand = beerInventoryService.getOnHandInventory(beer.getId());

      if (beer.getMinOnHand() >= inventoryQuantityOnHand) {
        log.debug("Beer {} ({}) low inventory. Min on hand = {} and current inventory = {}. Sending 'brewing " +
                "request' event",
            beer.getBeerName(), beer.getUpc(), beer.getMinOnHand(), inventoryQuantityOnHand);

        BeerEvent event = new BrewBeerEvent(beerMapper.beerToBeerDto(beer));

        jmsTemplate.convertAndSend(JmsConfig.BREWING_REQUEST_QUEUE, event);
      }
    }
  }
}
