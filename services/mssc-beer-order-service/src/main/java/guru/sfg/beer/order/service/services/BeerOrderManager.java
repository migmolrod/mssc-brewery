package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.brewery.model.BeerOrderDto;

import java.util.UUID;

public interface BeerOrderManager {

  BeerOrder newBeerOrder(BeerOrder beerOrder);

  void processBeerOrderValidation(UUID beerOrderId, Boolean valid);

  void processBeerOrderAllocationApproved(BeerOrderDto beerOrderDto);

  void processBeerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto);

  void processBeerOrderAllocationFailed(BeerOrderDto beerOrderDto);

  void pickUpOrder(UUID beerOrderId);
}
