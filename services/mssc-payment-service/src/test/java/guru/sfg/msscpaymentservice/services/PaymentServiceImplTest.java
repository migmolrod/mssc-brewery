package guru.sfg.msscpaymentservice.services;

import guru.sfg.msscpaymentservice.domain.Payment;
import guru.sfg.msscpaymentservice.domain.PaymentEvent;
import guru.sfg.msscpaymentservice.domain.PaymentState;
import guru.sfg.msscpaymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PaymentServiceImplTest {

  @Autowired
  PaymentService paymentService;

  @Autowired
  PaymentRepository paymentRepository;

  Payment payment;

  @BeforeEach
  void setUp() {
    payment = Payment.builder()
        .amount(new BigDecimal("12.99"))
        .build();
  }

  @Transactional
  @Test
  void preAuthorize() {
    Payment savedPayment = paymentService.newPayment(payment);

    StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.preAuthorize(savedPayment.getId());
    Payment preAuthorizedPayment = paymentRepository.getOne(savedPayment.getId());

    assertNotNull(stateMachine);
    // Cannot assert state because an Action randomizes output
    assertEquals(preAuthorizedPayment.getId(), savedPayment.getId());
  }

  @Transactional
  @Test
  void acceptPreAuth() {
    Payment savedPayment = paymentService.newPayment(payment);

    StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.acceptPreAuth(savedPayment.getId());
    Payment preAuthorizedPayment = paymentRepository.getOne(savedPayment.getId());

    assertNotNull(stateMachine);
    assertEquals(PaymentState.PRE_AUTH, preAuthorizedPayment.getState());
    assertEquals(preAuthorizedPayment.getId(), savedPayment.getId());
  }

  @Transactional
  @Test
  void declinePreAuth() {
    Payment savedPayment = paymentService.newPayment(payment);

    StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.declinePreAuth(savedPayment.getId());
    Payment preAuthorizedPayment = paymentRepository.getOne(savedPayment.getId());

    assertNotNull(stateMachine);
    assertEquals(PaymentState.PRE_AUTH_ERROR, preAuthorizedPayment.getState());
    assertEquals(preAuthorizedPayment.getId(), savedPayment.getId());
  }

  @Transactional
  @Test
  void authorize() {
    Payment savedPayment = paymentService.preAuthorizedPayment(payment);

    StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.authorize(savedPayment.getId());
    Payment preAuthorizedPayment = paymentRepository.getOne(savedPayment.getId());

    assertNotNull(stateMachine);
    // Cannot assert state because an Action randomizes output
    assertEquals(preAuthorizedPayment.getId(), savedPayment.getId());
  }

  @Transactional
  @Test
  void acceptAuth() {
    Payment savedPayment = paymentService.preAuthorizedPayment(payment);

    StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.acceptAuth(savedPayment.getId());
    Payment preAuthorizedPayment = paymentRepository.getOne(savedPayment.getId());

    assertNotNull(stateMachine);
    assertEquals(PaymentState.AUTH, preAuthorizedPayment.getState());
    assertEquals(preAuthorizedPayment.getId(), savedPayment.getId());
  }

  @Transactional
  @Test
  void declineAuth() {
    Payment savedPayment = paymentService.preAuthorizedPayment(payment);

    StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.declineAuth(savedPayment.getId());
    Payment preAuthorizedPayment = paymentRepository.getOne(savedPayment.getId());

    assertNotNull(stateMachine);
    assertEquals(PaymentState.AUTH_ERROR, preAuthorizedPayment.getState());
    assertEquals(preAuthorizedPayment.getId(), savedPayment.getId());
  }

}
