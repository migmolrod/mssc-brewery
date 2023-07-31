package guru.sfg.beer.order.service.services.inventory;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.events.AllocateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BeerOrderAllocationListener {

  private final BeerOrderManager beerOrderManager;

  @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
  public void listen(@Payload AllocateOrderResponse response) {
    if (response.getAllocationError()) {
      beerOrderManager.processBeerOrderAllocationFailed(response.getBeerOrder());
    } else if (response.getPendingInventory()) {
      beerOrderManager.processBeerOrderAllocationPendingInventory(response.getBeerOrder());
    } else {
      beerOrderManager.processBeerOrderAllocationApproved(response.getBeerOrder());
    }
  }
}
