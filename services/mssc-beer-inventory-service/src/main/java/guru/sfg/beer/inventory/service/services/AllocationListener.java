package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.inventory.service.config.JmsConfig;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AllocationListener {

  private final AllocationService allocationService;

  @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
  public void listen(AllocateOrderRequest request) {
    BeerOrderDto beerOrderDto = request.getBeerOrder();

    if (allocationService.allocateOrder(beerOrderDto)) {
      // SEND APPROVE_ALLOCATION
    } else {
      // SEND DENY_ALLOCATION
    }
  }
}
