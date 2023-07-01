package guru.sfg.msscbeerservice.web.mappers;

import guru.sfg.msscbeerservice.domain.Beer;
import guru.sfg.msscbeerservice.web.model.BeerDto;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

/**
 * Created by jt on 2019-05-25.
 */
@Mapper(uses = {DateMapper.class})
@DecoratedWith(BeerMapperDecorator.class)
public interface BeerMapper {

  BeerDto beerToBeerDto(Beer beer);

  BeerDto beerToBeerDtoExtra(Beer beer);

  Beer beerDtoToBeer(BeerDto dto);
}
