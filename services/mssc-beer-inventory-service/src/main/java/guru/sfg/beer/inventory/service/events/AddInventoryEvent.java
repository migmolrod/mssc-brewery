package guru.sfg.beer.inventory.service.events;

import guru.sfg.beer.inventory.service.web.model.BeerDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AddInventoryEvent extends BeerEvent {

  static final long serialVersionUID = -5064915618529791031L;

  public AddInventoryEvent(BeerDto beerDto) {
    super(beerDto);
  }

}
