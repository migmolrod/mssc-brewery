package guru.sfg.brewery.model.events;

import guru.sfg.brewery.model.BeerDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BrewBeerEvent extends BeerEvent {

  static final long serialVersionUID = -9183738435290209482L;

  public BrewBeerEvent(BeerDto beerDto) {
    super(beerDto);
  }

}
