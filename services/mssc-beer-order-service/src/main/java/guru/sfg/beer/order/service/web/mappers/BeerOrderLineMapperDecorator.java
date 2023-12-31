package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.services.beer.BeerService;
import guru.sfg.brewery.model.BeerDto;
import guru.sfg.brewery.model.BeerOrderLineDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public abstract class BeerOrderLineMapperDecorator implements BeerOrderLineMapper {

  private BeerService beerService;
  private BeerOrderLineMapper mapper;

  @Autowired
  public void setBeerService(BeerService beerService) {
    this.beerService = beerService;
  }

  @Autowired
  public void setMapper(BeerOrderLineMapper beerOrderLineMapper) {
    this.mapper = beerOrderLineMapper;
  }

  @Override
  public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
    BeerOrderLineDto beerOrderLineDto = mapper.beerOrderLineToDto(line);
    Optional<BeerDto> beerDtoOptional = beerService.getBeerByUpc(line.getUpc());

    beerDtoOptional.ifPresent(beerDto -> {
      beerOrderLineDto.setBeerId(beerDto.getId());
      beerOrderLineDto.setBeerName(beerDto.getBeerName());
      beerOrderLineDto.setBeerStyle(beerDto.getBeerStyle());
      beerOrderLineDto.setPrice(beerDto.getPrice());
    });

    return beerOrderLineDto;
  }

  @Override
  public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
    return mapper.dtoToBeerOrderLine(dto);
  }
}
