package guru.sfg.msscpaymentservice.services;

import guru.sfg.msscpaymentservice.domain.Payment;
import guru.sfg.msscpaymentservice.domain.PaymentEvent;
import guru.sfg.msscpaymentservice.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {

  Payment newPayment(Payment payment);

  Payment preAuthorizedPayment(Payment payment);

  StateMachine<PaymentState, PaymentEvent> preAuthorize(Long paymentId);

  StateMachine<PaymentState, PaymentEvent> acceptPreAuth(Long paymentId);

  StateMachine<PaymentState, PaymentEvent> declinePreAuth(Long paymentId);

  StateMachine<PaymentState, PaymentEvent> authorize(Long paymentId);

  StateMachine<PaymentState, PaymentEvent> acceptAuth(Long paymentId);

  StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);
}
