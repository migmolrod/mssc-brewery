package guru.sfg.msscpaymentservice.config;

import guru.sfg.msscpaymentservice.domain.PaymentEvent;
import guru.sfg.msscpaymentservice.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class StateMachineConfigTest {

  @Autowired
  StateMachineFactory<PaymentState, PaymentEvent> factory;

  @Test
  void testHappyPath() {
    StateMachine<PaymentState, PaymentEvent> stateMachine = factory.getStateMachine();

    stateMachine.start();
    assertEquals(stateMachine.getState().getId(), PaymentState.NEW);

    stateMachine.sendEvent(PaymentEvent.PRE_AUTHORIZE);
    assertEquals(stateMachine.getState().getId(), PaymentState.NEW);

    stateMachine.sendEvent(PaymentEvent.APPROVE_PRE_AUTHORIZE);
    assertEquals(stateMachine.getState().getId(), PaymentState.PRE_AUTH);

    stateMachine.sendEvent(PaymentEvent.AUTHORIZE);
    assertEquals(stateMachine.getState().getId(), PaymentState.PRE_AUTH);

    stateMachine.sendEvent(PaymentEvent.APPROVE_AUTHORIZE);
    assertEquals(stateMachine.getState().getId(), PaymentState.AUTH);
  }

  @Test
  void testErrorInPreAuth() {
    StateMachine<PaymentState, PaymentEvent> stateMachine = factory.getStateMachine();

    stateMachine.start();
    assertEquals(stateMachine.getState().getId(), PaymentState.NEW);

    stateMachine.sendEvent(PaymentEvent.PRE_AUTHORIZE);
    assertEquals(stateMachine.getState().getId(), PaymentState.NEW);

    stateMachine.sendEvent(PaymentEvent.DECLINE_PRE_AUTHORIZE);
    assertEquals(stateMachine.getState().getId(), PaymentState.PRE_AUTH_ERROR);
  }

  @Test
  void testErrorInAuth() {
    StateMachine<PaymentState, PaymentEvent> stateMachine = factory.getStateMachine();

    stateMachine.start();
    assertEquals(stateMachine.getState().getId(), PaymentState.NEW);

    stateMachine.sendEvent(PaymentEvent.PRE_AUTHORIZE);
    assertEquals(stateMachine.getState().getId(), PaymentState.NEW);

    stateMachine.sendEvent(PaymentEvent.APPROVE_PRE_AUTHORIZE);
    assertEquals(stateMachine.getState().getId(), PaymentState.PRE_AUTH);

    stateMachine.sendEvent(PaymentEvent.AUTHORIZE);
    assertEquals(stateMachine.getState().getId(), PaymentState.PRE_AUTH);

    stateMachine.sendEvent(PaymentEvent.DECLINE_AUTHORIZE);
    assertEquals(stateMachine.getState().getId(), PaymentState.AUTH_ERROR);
  }
}
