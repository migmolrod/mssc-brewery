package guru.sfg.msscbeerservice.web.mappers;

import guru.sfg.msscbeerservice.domain.Beer;
import guru.sfg.msscbeerservice.services.inventory.BeerInventoryService;
import guru.sfg.msscbeerservice.web.model.BeerDto;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BeerMapperDecorator implements BeerMapper {

  private BeerInventoryService beerInventoryService;
  private BeerMapper mapper;

  @Autowired
  public void setBeerInventoryService(BeerInventoryService beerInventoryService) {
    this.beerInventoryService = beerInventoryService;
  }

  @Autowired
  public void setMapper(BeerMapper beerMapper) {
    this.mapper = beerMapper;
  }

  @Override
  public BeerDto beerToBeerDto(Beer beer) {
    BeerDto dto = mapper.beerToBeerDto(beer);
    dto.setQuantityOnHand(beerInventoryService.getOnHandInventory(beer.getId()));

    return dto;
  }

  @Override
  public Beer beerDtoToBeer(BeerDto dto) {
    return mapper.beerDtoToBeer(dto);
  }
}
