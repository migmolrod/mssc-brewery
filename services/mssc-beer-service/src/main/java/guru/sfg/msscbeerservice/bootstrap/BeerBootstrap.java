package guru.sfg.msscbeerservice.bootstrap;

import guru.sfg.msscbeerservice.domain.Beer;
import guru.sfg.msscbeerservice.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerBootstrap implements CommandLineRunner {

  public static final String BEER_1_UPC = "0631234200036";
  public static final String BEER_2_UPC = "0631234300019";
  public static final String BEER_3_UPC = "0083783375213";

  private final BeerRepository beerRepository;

  @Override
  public void run(String... args) {
    loadBeerData();
  }

  private void loadBeerData() {
    if (beerRepository.count() == 0) {
      Beer savedBeer1 = beerRepository.save(Beer
          .builder()
          .beerName("Mango Bobs")
          .beerStyle("IPA")
          .minOnHand(41)
          .quantityToBrew(11)
          .upc(BEER_1_UPC)
          .price(new BigDecimal("12.95"))
          .build());
      log.debug("Created beer with id {}", savedBeer1.getId());

      Beer savedBeer2 = beerRepository.save(Beer
          .builder()
          .beerName("Galaxy Cat")
          .beerStyle("PALE_ALE")
          .minOnHand(42)
          .quantityToBrew(12)
          .upc(BEER_2_UPC)
          .price(new BigDecimal("8.99"))
          .build());
      log.debug("Created beer with id {}", savedBeer2.getId());

      Beer savedBeer3 = beerRepository.save(Beer
          .builder()
          .beerName("Pinball Porter")
          .beerStyle("PORTER")
          .minOnHand(43)
          .quantityToBrew(13)
          .upc(BEER_3_UPC)
          .price(new BigDecimal("5.25"))
          .build());
      log.debug("Created beer with id {}", savedBeer3.getId());
    }
  }
}
