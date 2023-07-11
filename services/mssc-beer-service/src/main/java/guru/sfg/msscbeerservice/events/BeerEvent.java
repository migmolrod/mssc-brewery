package guru.sfg.msscbeerservice.events;

import guru.sfg.msscbeerservice.web.model.BeerDto;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
@Builder
public class BeerEvent implements Serializable {

  static final long serialVersionUID = -937572347314083876L;

  private final BeerDto beerDto;
}
