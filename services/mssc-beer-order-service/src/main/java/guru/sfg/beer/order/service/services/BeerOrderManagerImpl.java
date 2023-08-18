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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

  public static final String BEER_ORDER_ID_HEADER = "ORDER_ID";
  public static final Integer MAX_AWAIT_FOR_STATUS_RETRIES = 10;
  public static final Integer AWAIT_DELAY_PER_RETRY = 50;

  private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
  private final BeerOrderRepository beerOrderRepository;
  private final BeerOrderStatusChangeInterceptor beerOrderStateChangeInterceptor;

  @Override
  @Transactional
  public BeerOrder newBeerOrder(BeerOrder beerOrder) {
    beerOrder.setId(null);
    beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

    BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);
    sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.START_VALIDATION);

    return savedBeerOrder;
  }

  @Override
  @Transactional
  public void processBeerOrderValidation(UUID beerOrderId, Boolean valid) {
    awaitForStatus(beerOrderId, BeerOrderStatusEnum.PENDING_VALIDATION);

    Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);

    beerOrderOptional.ifPresentOrElse(beerOrder -> {
          log.info("BOV - Order found. Id {}", beerOrderId);
          if (valid) {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.APPROVE_VALIDATION);

            // Read again beer order since the above `beerOrder` variable will be stale
            // and Hibernate may have trouble actually using it
            Optional<BeerOrder> validatedBeerOrder = beerOrderRepository.findById(beerOrderId);

            validatedBeerOrder.ifPresentOrElse(order -> {
                  sendBeerOrderEvent(order, BeerOrderEventEnum.START_ALLOCATION);
                  if (!order.equals(beerOrder)) {
                    log.error("orders do not match.");
                    log.error("{}", order);
                    log.error("{}", beerOrder);
                  }
                },
                () -> log.error("BOV - validated order not found. Id {}", beerOrderId));
          } else {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.DENY_VALIDATION);
          }
        },
        () -> log.error("BOV - Order not found. Id {}", beerOrderId)
    );
  }

  @Override
  @Transactional
  public void processBeerOrderAllocationApproved(BeerOrderDto beerOrderDto) {
    Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

    beerOrderOptional.ifPresentOrElse(beerOrder -> {
          log.info("BOAA - Order found. Id {}", beerOrder.getId());
          sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.APPROVE_ALLOCATION);
          updateAllocatedQty(beerOrderDto);
        },
        () -> log.error("BOAA - Order not found. Id {}", beerOrderDto.getId())
    );
  }

  @Override
  @Transactional
  public void processBeerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
    Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

    beerOrderOptional.ifPresentOrElse(beerOrder -> {
          log.info("BOAPI - Order found. Id {}", beerOrder.getId());
          sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.CONTINUE_ALLOCATION);
          updateAllocatedQty(beerOrderDto);
        },
        () -> log.error("BOAPI - Order not found. Id {}", beerOrderDto.getId())
    );
  }

  @Override
  @Transactional
  public void processBeerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
    Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

    beerOrderOptional.ifPresentOrElse(beerOrder -> {
          log.info("BOAF - Order found. Id {}", beerOrder.getId());
          sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.DENY_ALLOCATION);
        },
        () -> log.error("BOAF - Order not found. Id {}", beerOrderDto.getId())
    );
  }

  @Override
  @Transactional
  public void pickUpOrder(UUID beerOrderId) {
    beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
          log.info("BOPU - Order found. Id {}", beerOrder.getId());
          sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.PICK_UP);
        },
        () -> log.error("BOPU - Order not found. Id {}", beerOrderId)
    );
  }

  @Override
  @Transactional
  public void cancelOrder(UUID beerOrderId) {
    beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
          log.info("CO - Order found. Id {}", beerOrder.getId());
          sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.CANCEL_ORDER);
        },
        () -> log.error("CO - Order not found. Id {}", beerOrderId));
  }

  private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum event) {
    StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = buildStateMachine(beerOrder);

    Message<BeerOrderEventEnum> message = MessageBuilder.withPayload(event)
        .setHeader(BEER_ORDER_ID_HEADER, beerOrder.getId().toString())
        .build();

    stateMachine.sendEvent(message);
  }

  private void updateAllocatedQty(BeerOrderDto beerOrderDto) {
    Optional<BeerOrder> allocatedOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

    allocatedOrderOptional.ifPresentOrElse(allocatedOrder -> {
          log.info("BOUAQ (priv) - Order found. Id {}", allocatedOrder.getId());

          allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
            beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
              if (beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
                beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
              }
            });
          });

          beerOrderRepository.saveAndFlush(allocatedOrder);
        },
        () -> log.error("BOUAQ (priv) - Order not found. Id {}", beerOrderDto.getId())
    );
  }

  private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> buildStateMachine(BeerOrder beerOrder) {
    StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = stateMachineFactory.getStateMachine(beerOrder.getId());

    sm.stop();

    sm.getStateMachineAccessor()
        .doWithAllRegions(sma -> {
          sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
          sma.resetStateMachine(
              new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null)
          );
        });

    sm.start();

    return sm;
  }

  private void awaitForStatus(UUID beerOrderId, BeerOrderStatusEnum status) {
    AtomicBoolean found = new AtomicBoolean(false);
    AtomicInteger loopCount = new AtomicInteger(0);

    while (!found.get()) {
      log.warn("AFS - current loop count: {}", loopCount.get());
      if (loopCount.incrementAndGet() > MAX_AWAIT_FOR_STATUS_RETRIES) {
        found.set(true);
        log.warn("AFS - Max loop retries exceeded: {}", MAX_AWAIT_FOR_STATUS_RETRIES);
      }

      beerOrderRepository.findOneByIdAndOrderStatus(beerOrderId, status).ifPresentOrElse(beerOrder -> {
        found.set(true);
        log.info("AFS - Order {} found with status {}", beerOrderId, status.name());
      }, () -> {
        log.info("AFS - Order {} not found yet", beerOrderId);
      });

      if (!found.get()) {
        try {
          log.info("AFS - Retry {} awaiting for order {} to be in status {}", loopCount.get() + 1, beerOrderId,
              status.name());
          //noinspection BusyWait
          Thread.sleep(AWAIT_DELAY_PER_RETRY);
        } catch (Exception ignored) {}
      }
    }
  }

}
