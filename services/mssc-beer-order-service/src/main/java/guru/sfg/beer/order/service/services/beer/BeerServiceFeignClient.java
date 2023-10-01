package guru.sfg.beer.order.service.services.beer;

import guru.sfg.brewery.model.BeerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;
import java.util.UUID;

@FeignClient(name = "beer-service")
public interface BeerServiceFeignClient {

  @GetMapping(value = BeerServiceRestTemplateImpl.URI_BEER_BY_ID)
  ResponseEntity<Optional<BeerDto>> getBeerByUuid(@PathVariable UUID beerId);

  @GetMapping(value = BeerServiceRestTemplateImpl.URI_BEER_BY_UPC)
  ResponseEntity<Optional<BeerDto>> getBeerByUpc(@PathVariable String beerUpc);
}
