package guru.sfg.msscbeerservice.events;

import guru.sfg.msscbeerservice.web.model.BeerDto;

public class AddInventoryEvent extends BeerEvent {

  public AddInventoryEvent(BeerDto beerDto) {
    super(beerDto);
  }

}
