package guru.sfg.beer.inventory.service.web.controllers;

import guru.sfg.brewery.model.BeerInventoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Created by jt on 2019-05-31.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class BeerInventoryController {

  @GetMapping("api/v1/inventory-failover")
  List<BeerInventoryDto> listBeersById() {
    log.debug("Finding Inventory (failover)");

    return new ArrayList<>(Collections.singletonList(BeerInventoryDto
        .builder()
        .id(UUID.randomUUID())
        .beerId(new UUID(0L, 0L))
        .quantityOnHand(999)
        .createdDate(OffsetDateTime.now())
        .lastModifiedDate(OffsetDateTime.now())
        .build()));
  }
}
