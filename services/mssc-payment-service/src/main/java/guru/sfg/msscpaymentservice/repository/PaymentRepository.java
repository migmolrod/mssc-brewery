package guru.sfg.msscpaymentservice.repository;

import guru.sfg.msscpaymentservice.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
