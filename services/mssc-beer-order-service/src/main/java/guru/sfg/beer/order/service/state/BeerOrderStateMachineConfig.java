package guru.sfg.beer.order.service.state;

import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@RequiredArgsConstructor
@EnableStateMachineFactory
@Configuration
public class BeerOrderStateMachineConfig extends StateMachineConfigurerAdapter<BeerOrderStatusEnum,
    BeerOrderEventEnum> {

  private final Action<BeerOrderStatusEnum, BeerOrderEventEnum> validateOrderAction;
  private final Action<BeerOrderStatusEnum, BeerOrderEventEnum> allocateOrderAction;
  private final Action<BeerOrderStatusEnum, BeerOrderEventEnum> validationFailureAction;
  private final Action<BeerOrderStatusEnum, BeerOrderEventEnum> allocationFailureAction;
  private final Action<BeerOrderStatusEnum, BeerOrderEventEnum> deallocateOrderAction;

  @Override
  public void configure(StateMachineStateConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> states) throws Exception {
    states.withStates()
        .initial(BeerOrderStatusEnum.NEW)
        .states(EnumSet.allOf(BeerOrderStatusEnum.class))
        .end(BeerOrderStatusEnum.PICKED_UP)
        .end(BeerOrderStatusEnum.DELIVERED)
        .end(BeerOrderStatusEnum.CANCELLED)
        .end(BeerOrderStatusEnum.DELIVERY_FAILED)
        .end(BeerOrderStatusEnum.VALIDATION_DENIED)
        .end(BeerOrderStatusEnum.ALLOCATION_DENIED);
  }

  @Override
  public void configure(StateMachineTransitionConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> transitions) throws Exception {
    transitions.withExternal()
        /*
         * VALIDATION
         */
        .source(BeerOrderStatusEnum.NEW).target(BeerOrderStatusEnum.PENDING_VALIDATION)
        .event(BeerOrderEventEnum.START_VALIDATION)
        .action(validateOrderAction)

        .and().withExternal()
        .source(BeerOrderStatusEnum.PENDING_VALIDATION).target(BeerOrderStatusEnum.VALIDATION_APPROVED)
        .event(BeerOrderEventEnum.APPROVE_VALIDATION)

        .and().withExternal()
        .source(BeerOrderStatusEnum.PENDING_VALIDATION).target(BeerOrderStatusEnum.VALIDATION_DENIED)
        .event(BeerOrderEventEnum.DENY_VALIDATION)
        .action(validationFailureAction)

        /*
         * ALLOCATION
         */
        .and().withExternal()
        .source(BeerOrderStatusEnum.VALIDATION_APPROVED).target(BeerOrderStatusEnum.PENDING_ALLOCATION)
        .event(BeerOrderEventEnum.START_ALLOCATION)
        .action(allocateOrderAction)

        .and().withExternal()
        .source(BeerOrderStatusEnum.PENDING_ALLOCATION).target(BeerOrderStatusEnum.ALLOCATION_APPROVED)
        .event(BeerOrderEventEnum.APPROVE_ALLOCATION)

        .and().withExternal()
        .source(BeerOrderStatusEnum.PENDING_ALLOCATION).target(BeerOrderStatusEnum.ALLOCATION_DENIED)
        .event(BeerOrderEventEnum.DENY_ALLOCATION)
        .action(allocationFailureAction)

        .and().withExternal()
        .source(BeerOrderStatusEnum.PENDING_ALLOCATION).target(BeerOrderStatusEnum.ALLOCATION_PARTIAL)
        .event(BeerOrderEventEnum.CONTINUE_ALLOCATION)

        /*
         * PICKUP
         */
        .and().withExternal()
        .source(BeerOrderStatusEnum.ALLOCATION_APPROVED).target(BeerOrderStatusEnum.PICKED_UP)
        .event(BeerOrderEventEnum.PICK_UP)

        /*
         * CANCEL
         */
        .and().withExternal()
        .source(BeerOrderStatusEnum.PENDING_VALIDATION).target(BeerOrderStatusEnum.CANCELLED)
        .event(BeerOrderEventEnum.CANCEL_ORDER)

        .and().withExternal()
        .source(BeerOrderStatusEnum.VALIDATION_APPROVED).target(BeerOrderStatusEnum.CANCELLED)
        .event(BeerOrderEventEnum.CANCEL_ORDER)

        .and().withExternal()
        .source(BeerOrderStatusEnum.PENDING_ALLOCATION).target(BeerOrderStatusEnum.CANCELLED)
        .event(BeerOrderEventEnum.CANCEL_ORDER)

        .and().withExternal()
        .source(BeerOrderStatusEnum.ALLOCATION_APPROVED).target(BeerOrderStatusEnum.CANCELLED)
        .event(BeerOrderEventEnum.CANCEL_ORDER)
        .action(deallocateOrderAction)
    ;
  }
}
