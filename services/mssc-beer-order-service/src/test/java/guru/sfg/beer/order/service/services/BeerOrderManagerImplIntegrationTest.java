package guru.sfg.beer.order.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.services.beer.BeerServiceRestTemplateImpl;
import guru.sfg.brewery.model.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ExtendWith(WireMockExtension.class)
class BeerOrderManagerImplIntegrationTest {

  @Autowired
  BeerOrderManager beerOrderManager;

  @Autowired
  BeerOrderRepository beerOrderRepository;

  @Autowired
  CustomerRepository customerRepository;

  @Autowired
  WireMockServer wireMockServer;

  @Autowired
  ObjectMapper objectMapper;

  Customer testCustomer;

  UUID beerId = UUID.randomUUID();
  String beerUpc = "12345";

  @TestConfiguration
  static class RestTemplateBuilderProvider {
    @Bean(destroyMethod = "stop")
    public WireMockServer wireMockServer() {
      WireMockServer server = with(wireMockConfig().port(9091));
      server.start();

      return server;
    }
  }

  @BeforeEach
  void setUp() {
    testCustomer = customerRepository.saveAndFlush(
        Customer.builder()
            .customerName("Test Customer")
            .build());
  }

  @Test
  void testNewToAllocated() throws Exception {
    BeerDto beerDto = BeerDto.builder().id(beerId).upc(beerUpc).build();
//    BeerPagedList list = new BeerPagedList(List.of(beerDto));

    wireMockServer.stubFor(get("/" + BeerServiceRestTemplateImpl.URI_BEER_BY_UPC + "/" + beerUpc)
        .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    BeerOrder beerOrder = createBeerOrder();

    BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

    Thread.sleep(5000);

    BeerOrder savedBeerOrder2 = beerOrderRepository.findById(savedBeerOrder.getId()).get();

    assertNotNull(savedBeerOrder2);
    assertEquals(BeerOrderStatusEnum.ALLOCATION_APPROVED, savedBeerOrder2.getOrderStatus());
  }

  public BeerOrder createBeerOrder() {
    BeerOrder beerOrder = BeerOrder.builder().customer(testCustomer).build();

    Set<BeerOrderLine> lines = new HashSet<>();
    lines.add(BeerOrderLine.builder()
        .beerId(beerId)
        .upc(beerUpc)
        .orderQuantity(1)
        .beerOrder(beerOrder)
        .build());

    beerOrder.setBeerOrderLines(lines);

    return beerOrder;
  }

}
