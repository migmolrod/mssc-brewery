package guru.sfg.msscbeerservice.web.mappers;

import guru.sfg.msscbeerservice.domain.Beer;
import guru.sfg.msscbeerservice.services.inventory.InventoryService;
import guru.sfg.brewery.model.BeerDto;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BeerMapperDecorator implements BeerMapper {

  private InventoryService inventoryService;
  private BeerMapper mapper;

  @Autowired
  public void setBeerInventoryService(InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @Autowired
  public void setMapper(BeerMapper beerMapper) {
    this.mapper = beerMapper;
  }

  @Override
  public BeerDto beerToBeerDto(Beer beer) {
    return mapper.beerToBeerDto(beer);
  }

  @Override
  public BeerDto beerToBeerDtoExtra(Beer beer) {
    BeerDto dto = mapper.beerToBeerDto(beer);
    dto.setQuantityOnHand(inventoryService.getOnHandInventory(beer.getId()));

    return dto;
  }

  @Override
  public Beer beerDtoToBeer(BeerDto dto) {
    return mapper.beerDtoToBeer(dto);
  }
}
