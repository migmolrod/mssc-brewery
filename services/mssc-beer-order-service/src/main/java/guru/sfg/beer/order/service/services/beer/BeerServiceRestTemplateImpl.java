package guru.sfg.beer.order.service.services.beer;

import guru.sfg.brewery.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Profile("!local-discovery")
@Slf4j
@ConfigurationProperties(prefix= "sfg.brewery", ignoreUnknownFields = false)
@Component
public class BeerServiceRestTemplateImpl implements BeerService {

  public static final String URI_BEER_BY_ID = "api/v1/beer/{beerId}";
  public static final String URI_BEER_BY_UPC = "api/v1/beer/upc/{beerUpc}";

  private final RestTemplate restTemplate;

  private String beerServiceHost;

  public void setBeerServiceHost(String beerServiceHost) {
    this.beerServiceHost = beerServiceHost;
  }

  public BeerServiceRestTemplateImpl(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  @Override
  public Optional<BeerDto> getBeerByUuid(UUID beerId) {
    log.debug("GBBI - Calling 'Beer Service' from 'Order Service' using UUID {}", beerId);

    ResponseEntity<Optional<BeerDto>> responseEntity = restTemplate.exchange(
        beerServiceHost + "/" + URI_BEER_BY_ID,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<>() {},
        beerId
    );

    return responseEntity.getBody();
  }

  @Override
  public Optional<BeerDto> getBeerByUpc(String beerUpc) {
    log.debug("GBBU - Calling 'Beer Service' from 'Order Service' using UPC {}", beerUpc);

    ResponseEntity<Optional<BeerDto>> responseEntity = restTemplate.exchange(
        beerServiceHost + "/" + URI_BEER_BY_UPC,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<>() {},
        beerUpc
    );

    return responseEntity.getBody();
  }
}
