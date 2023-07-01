package guru.sfg.msscbeerservice.services;

import guru.sfg.msscbeerservice.web.model.BeerDto;
import guru.sfg.msscbeerservice.web.model.BeerPagedList;
import guru.sfg.msscbeerservice.web.model.BeerStyleEnum;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Created by jt on 2019-06-06.
 */
public interface BeerService {
  BeerPagedList listBeers(String beerName, BeerStyleEnum beerStyle, Boolean showInventory, Pageable pageable);

  BeerDto getById(UUID beerId, Boolean showInventory);

  BeerDto saveNewBeer(BeerDto beerDto);

  BeerDto getByUpc(String upc, Boolean showInventoryOnHand);

  BeerDto updateBeer(UUID beerId, BeerDto beerDto);
}
