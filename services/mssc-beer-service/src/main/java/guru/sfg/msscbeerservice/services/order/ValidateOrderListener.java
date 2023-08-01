package guru.sfg.msscbeerservice.services.order;

import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.ValidateOrderRequest;
import guru.sfg.brewery.model.events.ValidateOrderResponse;
import guru.sfg.msscbeerservice.config.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValidateOrderListener {

  private final BeerOrderValidator beerOrderValidator;
  private final JmsTemplate jmsTemplate;

  @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
  public void listen(@Payload ValidateOrderRequest validateOrderRequest) {
    BeerOrderDto beerOrder = validateOrderRequest.getBeerOrder();

    ValidateOrderResponse response = ValidateOrderResponse
        .builder()
        .beerOrderId(beerOrder.getId())
        .isValid(beerOrderValidator.validateOrder(beerOrder))
        .build();
    jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE, response);
  }
}
