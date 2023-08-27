package guru.sfg.beer.inventory.service.web.controllers;

import guru.sfg.brewery.model.BeerInventoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jt on 2019-05-31.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class BeerInventoryController {

  @GetMapping("api/v1/beer/{beerId}/inventory")
  List<BeerInventoryDto> listBeersById(@PathVariable UUID beerId) {
    log.debug("Finding Inventory for beer with id {}", beerId);
    List<BeerInventoryDto> beerInventoryList = new ArrayList<>();
    beerInventoryList.add(BeerInventoryDto
        .builder()
            .beerId(new UUID(0L, 0L))
            .quantityOnHand(999)
        .build());

    return beerInventoryList;
  }
}
