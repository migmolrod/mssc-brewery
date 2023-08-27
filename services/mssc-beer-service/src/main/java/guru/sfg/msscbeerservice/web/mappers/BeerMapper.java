package guru.sfg.msscbeerservice.web.mappers;

import guru.sfg.msscbeerservice.domain.Beer;
import guru.sfg.brewery.model.BeerDto;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
@DecoratedWith(BeerMapperDecorator.class)
public interface BeerMapper {

  BeerDto beerToBeerDto(Beer beer);

  BeerDto beerToBeerDtoExtra(Beer beer);

  Beer beerDtoToBeer(BeerDto dto);
}
