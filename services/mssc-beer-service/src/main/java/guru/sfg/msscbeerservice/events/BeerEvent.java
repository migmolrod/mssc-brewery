package guru.sfg.msscbeerservice.events;

import guru.sfg.msscbeerservice.web.model.BeerDto;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerEvent implements Serializable {

  static final long serialVersionUID = -937572347314083876L;

  private BeerDto beerDto;
}
