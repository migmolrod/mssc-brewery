package guru.sfg.msscpaymentservice.config;

import guru.sfg.msscpaymentservice.domain.PaymentEvent;
import guru.sfg.msscpaymentservice.domain.PaymentState;
import guru.sfg.msscpaymentservice.services.PaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

  @Override
  public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
    states.withStates()
        .initial(PaymentState.NEW)
        .states(EnumSet.allOf(PaymentState.class))
        .end(PaymentState.AUTH)
        .end(PaymentState.PRE_AUTH_ERROR)
        .end(PaymentState.AUTH_ERROR);
  }

  @Override
  public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
    transitions
        .withExternal().source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE).action(preAuthAction()).guard(paymentIdGuard())
        .and()
        .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.APPROVE_PRE_AUTHORIZE)
        .and()
        .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.DECLINE_PRE_AUTHORIZE)
        .and()
        .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH).event(PaymentEvent.AUTHORIZE).action(authAction())
        .and()
        .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH).event(PaymentEvent.APPROVE_AUTHORIZE)
        .and()
        .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR).event(PaymentEvent.DECLINE_AUTHORIZE)
    ;
  }

  @Override
  public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
    StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>() {
      @Override
      public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
        log.info("State changed from {} to {}",
            from != null ? from.getId() : "null",
            to != null ? to.getId() : "null");
      }
    };

    config.withConfiguration().listener(adapter);
  }

  public Guard<PaymentState, PaymentEvent> paymentIdGuard() {
    return stateContext -> stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
  }

  public Action<PaymentState, PaymentEvent> preAuthAction() {
    return stateContext -> {
      log.info("Pre auth was called");

      if (new Random().nextInt(10) < 5) {
        log.info("Pre auth approved");
        stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.APPROVE_PRE_AUTHORIZE)
            .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
            .build());
      } else {
        log.info("Pre auth denied");
        stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.DECLINE_PRE_AUTHORIZE)
            .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
            .build());
      }
    };
  }

  public Action<PaymentState, PaymentEvent> authAction() {
    return stateContext -> {
      log.info("Auth was called");

      if (new Random().nextInt(10) < 5) {
        log.info("Auth approved");
        stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.APPROVE_AUTHORIZE)
            .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
            .build());
      } else {
        log.info("Auth denied");
        stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.DECLINE_AUTHORIZE)
            .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
            .build());
      }
    };
  }
}
