package guru.sfg.msscpaymentservice.services;

import guru.sfg.msscpaymentservice.domain.Payment;
import guru.sfg.msscpaymentservice.domain.PaymentEvent;
import guru.sfg.msscpaymentservice.domain.PaymentState;
import guru.sfg.msscpaymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  public static final String PAYMENT_ID_HEADER = "payment_id";

  private final PaymentRepository paymentRepository;
  private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
  private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

  @Override
  public Payment newPayment(Payment payment) {
    payment.setState(PaymentState.NEW);

    return this.paymentRepository.save(payment);
  }

  @Override
  public Payment preAuthorizedPayment(Payment payment) {
    payment.setState(PaymentState.PRE_AUTH);

    return this.paymentRepository.save(payment);
  }

  @Transactional
  @Override
  public StateMachine<PaymentState, PaymentEvent> preAuthorize(Long paymentId) {
    StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

    sendEvent(paymentId, stateMachine, PaymentEvent.PRE_AUTHORIZE);

    return stateMachine;
  }

  @Transactional
  @Override
  public StateMachine<PaymentState, PaymentEvent> acceptPreAuth(Long paymentId) {
    StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

    sendEvent(paymentId, stateMachine, PaymentEvent.APPROVE_PRE_AUTHORIZE);

    return stateMachine;
  }

  @Transactional
  @Override
  public StateMachine<PaymentState, PaymentEvent> declinePreAuth(Long paymentId) {
    StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

    sendEvent(paymentId, stateMachine, PaymentEvent.DECLINE_PRE_AUTHORIZE);

    return stateMachine;
  }

  @Transactional
  @Override
  public StateMachine<PaymentState, PaymentEvent> authorize(Long paymentId) {
    StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

    sendEvent(paymentId, stateMachine, PaymentEvent.AUTHORIZE);

    return stateMachine;
  }

  @Transactional
  @Override
  public StateMachine<PaymentState, PaymentEvent> acceptAuth(Long paymentId) {
    StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

    sendEvent(paymentId, stateMachine, PaymentEvent.APPROVE_AUTHORIZE);

    return stateMachine;
  }

  @Transactional
  @Override
  public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
    StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

    sendEvent(paymentId, stateMachine, PaymentEvent.DECLINE_AUTHORIZE);

    return stateMachine;
  }

  private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> stateMachine, PaymentEvent event) {
    Message<PaymentEvent> message = MessageBuilder.withPayload(event)
        .setHeader(PAYMENT_ID_HEADER, paymentId)
        .build();

    stateMachine.sendEvent(message);
  }

  private StateMachine<PaymentState, PaymentEvent> build(Long paymentId) {
    Payment payment = this.paymentRepository.getOne(paymentId);
    StateMachine<PaymentState, PaymentEvent> stateMachine =
        stateMachineFactory.getStateMachine(Long.toString(payment.getId()));

    stateMachine.stop();

    stateMachine.getStateMachineAccessor()
        .doWithAllRegions(stateMachineAccessor -> {
          stateMachineAccessor.addStateMachineInterceptor(paymentStateChangeInterceptor);
          stateMachineAccessor.resetStateMachine(
              new DefaultStateMachineContext<>(payment.getState(), null, null , null)
          );
        });

    stateMachine.start();

    return stateMachine;
  }
}
