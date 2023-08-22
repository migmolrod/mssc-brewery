package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.brewery.model.CustomerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {DateMapper.class})
public interface CustomerMapper {

  @Mapping(target="name", source="customerName")
  CustomerDto modelToDto(Customer customer);

  @Mapping(target="customerName", source="name")
  Customer dtoToModel(CustomerDto customerDto);

}
