package guru.sfg.msscbeerservice.services.order;

import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.msscbeerservice.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidator {

  private final BeerRepository beerRepository;

  public Boolean validateOrder(BeerOrderDto beerOrderDto) {
    AtomicInteger beersNotFound = new AtomicInteger();

    beerOrderDto.getBeerOrderLines().forEach(line -> {
      if (beerRepository.findByUpc(line.getUpc()) == null) {
        beersNotFound.incrementAndGet();
      }
    });

    return beersNotFound.get() == 0;
  }
}
