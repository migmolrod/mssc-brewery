package guru.sfg.msscbeerservice.services;

import guru.sfg.brewery.model.BeerDto;
import guru.sfg.brewery.model.BeerPagedList;
import guru.sfg.brewery.model.BeerStyleEnum;
import guru.sfg.msscbeerservice.domain.Beer;
import guru.sfg.msscbeerservice.repositories.BeerRepository;
import guru.sfg.msscbeerservice.web.controller.NotFoundException;
import guru.sfg.msscbeerservice.web.mappers.BeerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by jt on 2019-06-06.
 */
@RequiredArgsConstructor
@Service
public class BeerServiceImpl implements BeerService {
  private final BeerRepository beerRepository;
  private final BeerMapper beerMapper;

  @Cacheable(cacheNames = "beer-list-cache", condition = "#showInventory == false")
  @Override
  public BeerPagedList listBeers(String beerName, BeerStyleEnum beerStyle, Boolean showInventory, Pageable pageable) {
    Page<Beer> beerPage;
    BeerPagedList beerPagedList;

    if (!ObjectUtils.isEmpty(beerName) && !ObjectUtils.isEmpty(beerStyle)) {
      beerPage = beerRepository.findAllByBeerNameAndBeerStyle(beerName, beerStyle.name(), pageable);
    } else if (!ObjectUtils.isEmpty(beerName) && ObjectUtils.isEmpty(beerStyle)) {
      beerPage = beerRepository.findAllByBeerName(beerName, pageable);
    } else if (ObjectUtils.isEmpty(beerName) && !ObjectUtils.isEmpty(beerStyle)) {
      beerPage = beerRepository.findAllByBeerStyle(beerStyle.name(), pageable);
    } else {
      beerPage = beerRepository.findAll(pageable);
    }

    if (Boolean.TRUE.equals(showInventory)) {
      beerPagedList = new BeerPagedList(
          beerPage
              .getContent()
              .stream()
              .map(beerMapper::beerToBeerDtoExtra)
              .collect(Collectors.toList()),
          PageRequest.of(beerPage.getPageable().getPageNumber(), beerPage.getPageable().getPageSize()),
          beerPage.getTotalElements()
      );
    } else {
      beerPagedList = new BeerPagedList(
          beerPage
              .getContent()
              .stream()
              .map(beerMapper::beerToBeerDto)
              .collect(Collectors.toList()),
          PageRequest.of(beerPage.getPageable().getPageNumber(), beerPage.getPageable().getPageSize()),
          beerPage.getTotalElements()
      );
    }

    return beerPagedList;
  }

  @Cacheable(cacheNames = "beer-uuid-cache", key="#beerId", condition = "#showInventory == false")
  @Override
  public BeerDto getById(UUID beerId, Boolean showInventory) {
    if (Boolean.TRUE.equals(showInventory)) {
      return beerMapper.beerToBeerDtoExtra(
          beerRepository.findById(beerId).orElseThrow(NotFoundException::new)
      );
    } else {
      return beerMapper.beerToBeerDto(
          beerRepository.findById(beerId).orElseThrow(NotFoundException::new)
      );
    }
  }

  @Cacheable(cacheNames = "beer-upc-cache", key="#upc", condition = "#showInventoryOnHand == false")
  @Override
  public BeerDto getByUpc(String upc, Boolean showInventoryOnHand) {
    if (Boolean.TRUE.equals(showInventoryOnHand)) {
      return beerMapper.beerToBeerDtoExtra(
          beerRepository.findByUpc(upc)
      );
    } else {
      return beerMapper.beerToBeerDto(
          beerRepository.findByUpc(upc)
      );
    }
  }

  @Override
  public BeerDto saveNewBeer(BeerDto beerDto) {
    return beerMapper.beerToBeerDto(beerRepository.save(beerMapper.beerDtoToBeer(beerDto)));
  }

  @Override
  public BeerDto updateBeer(UUID beerId, BeerDto beerDto) {
    Beer beer = beerRepository.findById(beerId).orElseThrow(NotFoundException::new);

    beer.setBeerName(beerDto.getBeerName());
    beer.setBeerStyle(beerDto.getBeerStyle());
    beer.setPrice(beerDto.getPrice());
    beer.setUpc(beerDto.getUpc());

    return beerMapper.beerToBeerDto(beerRepository.save(beer));
  }
}
