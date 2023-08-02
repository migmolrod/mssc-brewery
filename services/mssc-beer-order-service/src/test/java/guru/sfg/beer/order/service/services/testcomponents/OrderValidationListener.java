package guru.sfg.beer.order.service.services.testcomponents;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.model.events.ValidateOrderRequest;
import guru.sfg.brewery.model.events.ValidateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderValidationListener {

  private final JmsTemplate jmsTemplate;

  @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
  public void listen(Message<?> msg) {
    boolean isValid = true;
    boolean sendResponse = true;

    ValidateOrderRequest request = (ValidateOrderRequest) msg.getPayload();

    if (request.getBeerOrder().getCustomerRef() != null) {
      switch (request.getBeerOrder().getCustomerRef()) {
        case "fail-validation":
          isValid = false;
          break;
        case "dont-validate":
          sendResponse = false;
          break;
      }
    }

    if (sendResponse) {
      jmsTemplate.convertAndSend(
          JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
          ValidateOrderResponse.builder()
              .isValid(isValid)
              .beerOrderId(request.getBeerOrder().getId())
              .build()
      );
    }
  }
}
