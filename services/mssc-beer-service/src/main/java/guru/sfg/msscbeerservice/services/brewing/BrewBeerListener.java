package guru.sfg.msscbeerservice.services.brewing;

import guru.sfg.msscbeerservice.config.JmsConfig;
import guru.sfg.msscbeerservice.domain.Beer;
import guru.sfg.brewery.model.events.AddInventoryEvent;
import guru.sfg.brewery.model.events.BeerEvent;
import guru.sfg.brewery.model.events.BrewBeerEvent;
import guru.sfg.msscbeerservice.repositories.BeerRepository;
import guru.sfg.brewery.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrewBeerListener {

  private final BeerRepository beerRepository;
  private final JmsTemplate jmsTemplate;

  @Transactional
  @JmsListener(destination = JmsConfig.BREWING_REQUEST_QUEUE)
  public void listen(BrewBeerEvent event) {
    BeerDto beerDto = event.getBeerDto();
    Beer beer = beerRepository.getOne(beerDto.getId());
    beerDto.setQuantityOnHand(beer.getQuantityToBrew());

    log.debug("Brewing event caught for beer {} ({}). Min on hand = {} and current inventory = {}. Sending 'add " +
            "inventory' event with quantity to brew {}",
        beer.getBeerName(), beer.getUpc(), beer.getMinOnHand(), beerDto.getQuantityOnHand(),
        beer.getQuantityToBrew());
    BeerEvent addInventoryEvent = new AddInventoryEvent(beerDto);

    jmsTemplate.convertAndSend(JmsConfig.ADD_INVENTORY_QUEUE, addInventoryEvent);
  }

}
