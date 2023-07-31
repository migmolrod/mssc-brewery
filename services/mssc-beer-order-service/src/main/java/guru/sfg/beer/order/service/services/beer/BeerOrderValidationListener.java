package guru.sfg.beer.order.service.services.beer;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.events.ValidateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BeerOrderValidationListener {

  private final BeerOrderManager beerOrderManager;

  @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
  public void listen(@Payload ValidateOrderResponse response) {
    beerOrderManager.processBeerOrderValidation(response.getBeerOrderId(), response.getIsValid());
  }
}
