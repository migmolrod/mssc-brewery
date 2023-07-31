package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.inventory.service.config.JmsConfig;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AllocationListener {

  private final AllocationService allocationService;
  private final JmsTemplate jmsTemplate;

  @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
  public void listen(AllocateOrderRequest request) {
    BeerOrderDto beerOrderDto = request.getBeerOrder();
    AllocateOrderResponse.AllocateOrderResponseBuilder response = AllocateOrderResponse
        .builder()
        .beerOrder(beerOrderDto);

    try {
      response.pendingInventory(!allocationService.allocateOrder(beerOrderDto));
    } catch (Exception exception) {
      response.allocationError(true);
      log.error("Exception allocating order {}. Exception: {}", beerOrderDto.getId(), exception.getMessage());
    }
    jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE, response.build());
  }
}
