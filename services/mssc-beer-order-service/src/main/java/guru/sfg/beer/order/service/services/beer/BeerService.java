package guru.sfg.beer.order.service.services.beer;

import guru.sfg.brewery.model.BeerDto;

import java.util.Optional;
import java.util.UUID;

public interface BeerService {
  Optional<BeerDto> getBeerByUuid(UUID beerId);

  Optional<BeerDto> getBeerByUpc(String upc);
}
