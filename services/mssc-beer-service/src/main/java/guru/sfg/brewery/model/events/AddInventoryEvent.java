package guru.sfg.brewery.model.events;

import guru.sfg.brewery.model.BeerDto;

public class AddInventoryEvent extends BeerEvent {

  static final long serialVersionUID = -5064915618529791031L;

  public AddInventoryEvent(BeerDto beerDto) {
    super(beerDto);
  }

}
