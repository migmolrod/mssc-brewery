package guru.sfg.msscpaymentservice.domain;

public enum PaymentEvent {
  PRE_AUTHORIZE, APPROVE_PRE_AUTHORIZE, DECLINE_PRE_AUTHORIZE,
  AUTHORIZE, APPROVE_AUTHORIZE, DECLINE_AUTHORIZE
}
