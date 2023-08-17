package guru.sfg.beer.order.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.services.beer.BeerServiceRestTemplateImpl;
import guru.sfg.brewery.model.BeerDto;
import guru.sfg.brewery.model.events.AllocateFailureEvent;
import guru.sfg.brewery.model.events.DeallocateOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
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
  JmsTemplate jmsTemplate;

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
    BeerDto beerDto = createBeer();

    wireMockServer.stubFor(get("/" + BeerServiceRestTemplateImpl.URI_BEER_BY_UPC + "/" + beerUpc)
        .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    BeerOrder beerOrder = createBeerOrder();
    BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      assertEquals(BeerOrderStatusEnum.ALLOCATION_APPROVED, foundOrder.getOrderStatus());
    });

    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      BeerOrderLine line = foundOrder.getBeerOrderLines().iterator().next();
      assertEquals(line.getOrderQuantity(), line.getQuantityAllocated());
    });

    BeerOrder allocatedBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

    assertNotNull(allocatedBeerOrder);
    assertEquals(BeerOrderStatusEnum.ALLOCATION_APPROVED, allocatedBeerOrder.getOrderStatus());
    allocatedBeerOrder.getBeerOrderLines().forEach(line -> {
      assertEquals(line.getOrderQuantity(), line.getQuantityAllocated());
    });
  }

  @Test
  void testValidationFailed() throws JsonProcessingException {
    BeerDto beerDto = createBeer();

    wireMockServer.stubFor(get("/" + BeerServiceRestTemplateImpl.URI_BEER_BY_UPC + "/" + beerUpc)
        .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    BeerOrder beerOrder = createBeerOrder();
    beerOrder.setCustomerRef("fail-validation");

    BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      assertEquals(BeerOrderStatusEnum.VALIDATION_DENIED, foundOrder.getOrderStatus());
    });

    BeerOrder deniedBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

    assertNotNull(deniedBeerOrder);
    assertEquals(BeerOrderStatusEnum.VALIDATION_DENIED, deniedBeerOrder.getOrderStatus());
  }

  @Test
  void testAllocationFailure() throws JsonProcessingException {
    BeerDto beerDto = createBeer();

    wireMockServer.stubFor(get("/" + BeerServiceRestTemplateImpl.URI_BEER_BY_UPC + "/" + beerUpc)
        .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    BeerOrder beerOrder = createBeerOrder();
    beerOrder.setCustomerRef("fail-allocation");

    BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      assertEquals(BeerOrderStatusEnum.ALLOCATION_DENIED, foundOrder.getOrderStatus());
    });

    AllocateFailureEvent allocateFailureEvent =
        (AllocateFailureEvent) jmsTemplate.receiveAndConvert(JmsConfig.ALLOCATE_FAILURE_QUEUE);

    assertNotNull(allocateFailureEvent);
    assertEquals(allocateFailureEvent.getBeerOrderId(), savedBeerOrder.getId());
  }

  @Test
  void testNewToPickedUp() throws JsonProcessingException {
    BeerDto beerDto = createBeer();

    wireMockServer.stubFor(get("/" + BeerServiceRestTemplateImpl.URI_BEER_BY_UPC + "/" + beerUpc)
        .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    BeerOrder beerOrder = createBeerOrder();

    BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      assertEquals(BeerOrderStatusEnum.ALLOCATION_APPROVED, foundOrder.getOrderStatus());
    });

    beerOrderManager.pickUpOrder(savedBeerOrder.getId());

    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
      assertEquals(BeerOrderStatusEnum.PICKED_UP, foundOrder.getOrderStatus());
    });

    BeerOrder pickedUpOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

    assertNotNull(pickedUpOrder);
    assertEquals(BeerOrderStatusEnum.PICKED_UP, pickedUpOrder.getOrderStatus());
  }

  @Test
  void testValidationPendingToCancelled() throws JsonProcessingException {
    BeerDto beerDto = createBeer();

    wireMockServer.stubFor(get("/" + BeerServiceRestTemplateImpl.URI_BEER_BY_UPC + "/" + beerUpc)
        .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    BeerOrder beerOrder = createBeerOrder();
    beerOrder.setCustomerRef("dont-validate");

    BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      assertEquals(BeerOrderStatusEnum.PENDING_VALIDATION, foundOrder.getOrderStatus());
    });

    beerOrderManager.cancelOrder(savedBeerOrder.getId());

    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      assertEquals(BeerOrderStatusEnum.CANCELLED, foundOrder.getOrderStatus());
    });

    BeerOrder cancelledOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

    assertNotNull(cancelledOrder);
    assertEquals(BeerOrderStatusEnum.CANCELLED, cancelledOrder.getOrderStatus());
  }

  @Test
  void testAllocationPendingToCancelled() throws JsonProcessingException {
    BeerDto beerDto = createBeer();

    wireMockServer.stubFor(get("/" + BeerServiceRestTemplateImpl.URI_BEER_BY_UPC + "/" + beerUpc)
        .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    BeerOrder beerOrder = createBeerOrder();
    beerOrder.setCustomerRef("dont-allocate");

    BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      assertEquals(BeerOrderStatusEnum.PENDING_ALLOCATION, foundOrder.getOrderStatus());
    });

    beerOrderManager.cancelOrder(savedBeerOrder.getId());

    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      assertEquals(BeerOrderStatusEnum.CANCELLED, foundOrder.getOrderStatus());
    });

    BeerOrder cancelledOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

    assertNotNull(cancelledOrder);
    assertEquals(BeerOrderStatusEnum.CANCELLED, cancelledOrder.getOrderStatus());
  }

  @Test
  void testAllocatedToCancelled() throws JsonProcessingException {
    BeerDto beerDto = createBeer();

    wireMockServer.stubFor(get("/" + BeerServiceRestTemplateImpl.URI_BEER_BY_UPC + "/" + beerUpc)
        .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    BeerOrder beerOrder = createBeerOrder();

    BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      assertEquals(BeerOrderStatusEnum.ALLOCATION_APPROVED, foundOrder.getOrderStatus());
    });

    beerOrderManager.cancelOrder(savedBeerOrder.getId());

    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      assertEquals(BeerOrderStatusEnum.CANCELLED, foundOrder.getOrderStatus());
    });

    BeerOrder cancelledOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
    assertNotNull(cancelledOrder);
    assertEquals(BeerOrderStatusEnum.CANCELLED, cancelledOrder.getOrderStatus());

    DeallocateOrderRequest allocateFailureEvent =
        (DeallocateOrderRequest) jmsTemplate.receiveAndConvert(JmsConfig.DEALLOCATE_ORDER_QUEUE);
    assertNotNull(allocateFailureEvent);
    assertEquals(allocateFailureEvent.getBeerOrder().getId(), savedBeerOrder.getId());
  }

  private BeerDto createBeer() {
    return BeerDto.builder().id(beerId).upc(beerUpc).build();
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
