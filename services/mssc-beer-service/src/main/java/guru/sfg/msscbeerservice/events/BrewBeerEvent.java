package guru.sfg.msscbeerservice.events;

import guru.sfg.msscbeerservice.web.model.BeerDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BrewBeerEvent extends BeerEvent {

  static final long serialVersionUID = -9183738435290209482L;

  public BrewBeerEvent(BeerDto beerDto) {
    super(beerDto);
  }

}
