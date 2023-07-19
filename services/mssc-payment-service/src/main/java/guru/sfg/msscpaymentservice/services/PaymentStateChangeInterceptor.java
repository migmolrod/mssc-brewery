package guru.sfg.msscpaymentservice.services;

import guru.sfg.msscpaymentservice.domain.Payment;
import guru.sfg.msscpaymentservice.domain.PaymentEvent;
import guru.sfg.msscpaymentservice.domain.PaymentState;
import guru.sfg.msscpaymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

  private final PaymentRepository paymentRepository;

  @Override
  public void preStateChange(State<PaymentState, PaymentEvent> state,
                             Message<PaymentEvent> message,
                             Transition<PaymentState, PaymentEvent> transition,
                             StateMachine<PaymentState, PaymentEvent> stateMachine) {
    Optional.ofNullable(message).flatMap(msg -> Optional.ofNullable((Long) message.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER,
        -1L))).ifPresent(paymentId -> {
      Payment payment = this.paymentRepository.getOne(paymentId);
      payment.setState(state.getId());
      paymentRepository.save(payment);
    });
  }
}
