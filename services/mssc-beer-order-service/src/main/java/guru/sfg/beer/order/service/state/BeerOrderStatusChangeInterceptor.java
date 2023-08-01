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
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class BeerOrderStatusChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatusEnum,
    BeerOrderEventEnum> {

  private final BeerOrderRepository beerOrderRepository;

  @Transactional
  @Override
  public void preStateChange(State<BeerOrderStatusEnum, BeerOrderEventEnum> state,
                             Message<BeerOrderEventEnum> message,
                             Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition,
                             StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine) {
    log.debug("Pre state change: state {} - message {}", state.getId(), message);

    Optional.ofNullable(message).flatMap(msg -> Optional.ofNullable((String) message.getHeaders().getOrDefault(BeerOrderManagerImpl.BEER_ORDER_ID_HEADER, -1L))).ifPresent(orderId -> {
      BeerOrder order = this.beerOrderRepository.getOne(UUID.fromString(orderId));
      order.setOrderStatus(state.getId());
      beerOrderRepository.save(order);
    });
  }
}
