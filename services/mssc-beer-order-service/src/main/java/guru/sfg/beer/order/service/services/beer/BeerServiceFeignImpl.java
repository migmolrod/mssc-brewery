package guru.sfg.beer.order.service.services.beer;

import guru.sfg.brewery.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Profile("local-discovery")
@Slf4j
@RequiredArgsConstructor
@Service
public class BeerServiceFeignImpl implements BeerService {

  private final BeerServiceFeignClient beerServiceFeignClient;

  @Override
  public Optional<BeerDto> getBeerByUuid(UUID beerId) {
    log.debug("GBBI - Calling 'Beer Service' from 'Order Service' with OpenFeign using UUID {}", beerId);

    ResponseEntity<Optional<BeerDto>> responseEntity = beerServiceFeignClient.getBeerByUuid(beerId);

    return Objects.requireNonNull(responseEntity.getBody());
  }

  @Override
  public Optional<BeerDto> getBeerByUpc(String beerUpc) {
    log.debug("GBBU - Calling 'Beer Service' from 'Order Service' with OpenFeign using UPC {}", beerUpc);

    ResponseEntity<Optional<BeerDto>> responseEntity = beerServiceFeignClient.getBeerByUpc(beerUpc);

    return Objects.requireNonNull(responseEntity.getBody());
  }
}
