package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.state.BeerOrderStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

  public static final String BEER_ORDER_ID_HEADER = "ORDER_ID";

  private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
  private final BeerOrderRepository beerOrderRepository;
  private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

  @Override
  @Transactional
  public BeerOrder newBeerOrder(BeerOrder beerOrder) {
    beerOrder.setId(null);
    beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

    BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);
    sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATE_ORDER);

    return savedBeerOrder;
  }

  @Override
  @Transactional
  public void processBeerOrderValidation(UUID beerOrderId, Boolean valid) {
    beerOrderRepository.findById(beerOrderId).ifPresent(beerOrder -> {
      if (valid) {
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.APPROVE_VALIDATION);
      } else {
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.DENY_VALIDATION);
      }
    });
  }

  private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum event) {
    StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = build(beerOrder);

    Message<BeerOrderEventEnum> message = MessageBuilder.withPayload(event)
        .setHeader(BEER_ORDER_ID_HEADER, beerOrder.getId())
        .build();

    stateMachine.sendEvent(message);
  }

  private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
    StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine =
        stateMachineFactory.getStateMachine(beerOrder.getId());

    stateMachine.stop();

    stateMachine.getStateMachineAccessor()
        .doWithAllRegions(stateMachineAccessor -> {
          stateMachineAccessor.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
          stateMachineAccessor.resetStateMachine(
              new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null)
          );
        });

    stateMachine.start();

    return stateMachine;
  }
}
