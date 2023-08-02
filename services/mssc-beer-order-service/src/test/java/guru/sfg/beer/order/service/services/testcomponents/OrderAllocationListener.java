package guru.sfg.beer.order.service.services.testcomponents;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderAllocationListener {

  private final JmsTemplate jmsTemplate;

  @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
  public void listen(Message<?> msg) {
    AllocateOrderRequest request = (AllocateOrderRequest) msg.getPayload();
    boolean pendingInventory = false;
    boolean allocationError = false;
    boolean sendResponse = true;

    if (request.getBeerOrder().getCustomerRef() != null) {
      switch (request.getBeerOrder().getCustomerRef()) {
        case "fail-allocation":
          allocationError = true;
          break;
        case "partial-allocation":
          pendingInventory = true;
          break;
        case "dont-allocate":
          sendResponse = false;
          break;
      }
    }

    boolean finalPendingInventory = pendingInventory;

    request.getBeerOrder().getBeerOrderLines().forEach(beerOrderLineDto -> {
      if (finalPendingInventory) {
        beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity() - 1);
      } else {
        beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
      }
    });

    if (sendResponse) {
      jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
          AllocateOrderResponse.builder()
              .beerOrder(request.getBeerOrder())
              .pendingInventory(pendingInventory)
              .allocationError(allocationError)
              .build());
    }
  }
}
