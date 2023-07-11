package guru.sfg.msscbeerservice.events;

import guru.sfg.msscbeerservice.web.model.BeerDto;

public class BrewBeerEvent extends BeerEvent {

  public BrewBeerEvent(BeerDto beerDto) {
    super(beerDto);
  }

}
