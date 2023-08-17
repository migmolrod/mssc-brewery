package guru.sfg.beer.inventory.service.services;

import guru.sfg.brewery.model.BeerOrderDto;

public interface DeallocationService {
  void deallocateOrder(BeerOrderDto beerOrderDto);
}
