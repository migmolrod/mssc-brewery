package guru.sfg.beer.order.service.state.actions;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

  private final BeerOrderRepository repository;
  private final BeerOrderMapper mapper;
  private final JmsTemplate jmsTemplate;

  @Override
  public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
    String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.BEER_ORDER_ID_HEADER);

    repository.findById(UUID.fromString(beerOrderId)).ifPresentOrElse(beerOrder -> {
          AllocateOrderRequest request = AllocateOrderRequest.builder()
              .beerOrder(mapper.beerOrderToDto(beerOrder))
              .build();
          jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_QUEUE, request);
          log.debug("Sent allocation request for order {}", beerOrderId);
        },
        () -> log.error("Beer order not found {}", beerOrderId));
  }

}
