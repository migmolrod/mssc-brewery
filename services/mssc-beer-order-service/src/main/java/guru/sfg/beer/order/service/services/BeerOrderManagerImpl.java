package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.state.BeerOrderStateChangeInterceptor;
import guru.sfg.brewery.model.BeerOrderDto;
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
    sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.START_VALIDATION);

    return savedBeerOrder;
  }

  @Override
  @Transactional
  public void processBeerOrderValidation(UUID beerOrderId, Boolean valid) {
    beerOrderRepository.findById(beerOrderId).ifPresent(beerOrder -> {
      if (valid) {
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.APPROVE_VALIDATION);

        // Read again beer order since the above `beerOrder` variable will be stale
        // and Hibernate may have trouble actually using it
        BeerOrder validatedBeerOrder = beerOrderRepository.getOne(beerOrderId);

        sendBeerOrderEvent(validatedBeerOrder, BeerOrderEventEnum.START_ALLOCATION);
      } else {
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.DENY_VALIDATION);
      }
    });
  }

  @Override
  @Transactional
  public void processBeerOrderAllocationApproved(BeerOrderDto beerOrderDto) {
    BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
    sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.APPROVE_ALLOCATION);
    updateAllocatedQty(beerOrderDto, beerOrder);
  }

  @Override
  @Transactional
  public void processBeerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
    BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
    sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
    updateAllocatedQty(beerOrderDto, beerOrder);
  }

  @Override
  @Transactional
  public void processBeerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
    BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
    sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.DENY_ALLOCATION);
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

  private void updateAllocatedQty(BeerOrderDto beerOrderDto, BeerOrder beerOrder) {
    BeerOrder allocatedOrder = beerOrderRepository.getOne(beerOrderDto.getId());

    allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
      beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
        if (beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
          beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
        }
      });
    });

    beerOrderRepository.saveAndFlush(allocatedOrder);
  }

}
