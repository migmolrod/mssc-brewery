package guru.sfg.beer.order.service.state;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class BeerOrderStatusChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatusEnum,
    BeerOrderEventEnum> {

  private final BeerOrderRepository beerOrderRepository;

  @Override
  @Transactional
  public void preStateChange(State<BeerOrderStatusEnum, BeerOrderEventEnum> state,
                             Message<BeerOrderEventEnum> message,
                             Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition,
                             StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine) {
    Optional.ofNullable(message)
        .flatMap(msg -> Optional.ofNullable((String) message.getHeaders().getOrDefault(BeerOrderManagerImpl.BEER_ORDER_ID_HEADER, -1L)))
        .ifPresentOrElse(
            orderId -> {
              Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(orderId));

              beerOrderOptional.ifPresentOrElse(order -> {
                    log.info("PSC - Transition order status for order {}, from {} to {}",
                        order.getId(),
                        order.getOrderStatus(),
                        state.getId());
                    order.setOrderStatus(state.getId());
                    BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(order);
                    log.info("PSC - Saved order {}, status in database {}, status from message {}",
                        savedBeerOrder.getId(),
                        savedBeerOrder.getOrderStatus(),
                        state.getId());
                  },
                  () -> log.error("PSC - Order not found. Id {}", orderId)
              );
            },
            () -> log.error("PSC - Error getting header {} from message", BeerOrderManagerImpl.BEER_ORDER_ID_HEADER));
  }
}
