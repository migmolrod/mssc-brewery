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
    ValidateOrderRequest request = (ValidateOrderRequest) msg.getPayload();

    String logMsg = "################################ RUN ok FROM TEST";
    log.warn(logMsg);
    System.out.println(logMsg);

    jmsTemplate.convertAndSend(
        JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
        ValidateOrderResponse.builder()
            .isValid(true)
            .beerOrderId(request.getBeerOrder().getId())
            .build()
        );
  }
}
