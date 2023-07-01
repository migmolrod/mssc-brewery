package guru.sfg.beer.order.service.services.beer;

import guru.sfg.beer.order.service.services.beer.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@ConfigurationProperties(prefix= "sfg.brewery", ignoreUnknownFields = false)
@Component
public class BeerServiceRestTemplateImpl implements BeerService {

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
    log.debug("Calling 'Beer Service' from 'Order Service' using UUID {}", beerId);

    String url = String.format("%s/%s/%s", beerServiceHost, "api/v1/beer", beerId);

    return Optional.ofNullable(
        restTemplate.getForObject(url, BeerDto.class)
    );
  }

  @Override
  public Optional<BeerDto> getBeerByUpc(String upc) {
    log.debug("Calling 'Beer Service' from 'Order Service' using UPC {}", upc);

    String url = String.format("%s/%s/%s", beerServiceHost, "api/v1/beer/upc", upc);

    return Optional.ofNullable(
        restTemplate.getForObject(url, BeerDto.class)
    );
  }
}
