package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.state.BeerOrderStatusChangeInterceptor;
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

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

  public static final String BEER_ORDER_ID_HEADER = "ORDER_ID";

  private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
  private final BeerOrderRepository beerOrderRepository;
  private final BeerOrderStatusChangeInterceptor beerOrderStateChangeInterceptor;

  @Override
  @Transactional
  public BeerOrder newBeerOrder(BeerOrder beerOrder) {
    beerOrder.setId(null);
    beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

    BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);
    sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.START_VALIDATION);

    return savedBeerOrder;
  }

  @Override
  @Transactional
  public void processBeerOrderValidation(UUID beerOrderId, Boolean valid) {
    Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);

    beerOrderOptional.ifPresentOrElse(beerOrder -> {
          if (valid) {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.APPROVE_VALIDATION);

            // Read again beer order since the above `beerOrder` variable will be stale
            // and Hibernate may have trouble actually using it
            Optional<BeerOrder> validatedBeerOrder = beerOrderRepository.findById(beerOrderId);

            validatedBeerOrder.ifPresent(order -> sendBeerOrderEvent(order, BeerOrderEventEnum.START_ALLOCATION));
          } else {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.DENY_VALIDATION);
          }
        },
        () -> log.error("Order not found. Id {}", beerOrderId)
    );
  }

  @Override
  @Transactional
  public void processBeerOrderAllocationApproved(BeerOrderDto beerOrderDto) {
    beerOrderRepository.findById(beerOrderDto.getId()).ifPresentOrElse(
        beerOrder -> {
          sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.APPROVE_ALLOCATION);
          updateAllocatedQty(beerOrderDto, beerOrder);
        },
        () -> log.error("Order not found. Id {}", beerOrderDto.getId())
    );
  }

  @Override
  @Transactional
  public void processBeerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
    beerOrderRepository.findById(beerOrderDto.getId()).ifPresentOrElse(
        beerOrder -> {
          sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
          updateAllocatedQty(beerOrderDto, beerOrder);
        },
        () -> log.error("Order not found. Id {}", beerOrderDto.getId())
    );
  }

  @Override
  @Transactional
  public void processBeerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
    beerOrderRepository.findById(beerOrderDto.getId()).ifPresentOrElse(
        beerOrder -> sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.DENY_ALLOCATION),
        () -> log.error("Order not found. Id {}", beerOrderDto.getId())
    );
  }

  private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum event) {
    StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = build(beerOrder);

    Message<BeerOrderEventEnum> message = MessageBuilder.withPayload(event)
        .setHeader(BEER_ORDER_ID_HEADER, beerOrder.getId().toString())
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
