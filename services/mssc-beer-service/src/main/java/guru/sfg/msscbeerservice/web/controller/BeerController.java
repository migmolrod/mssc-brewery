package guru.sfg.msscbeerservice.web.controller;

import guru.sfg.msscbeerservice.services.BeerService;
import guru.sfg.msscbeerservice.web.model.BeerDto;
import guru.sfg.msscbeerservice.web.model.BeerPagedList;
import guru.sfg.msscbeerservice.web.model.BeerStyleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Created by jt on 2019-05-12.
 */
@RequiredArgsConstructor
@RequestMapping("/api/v1/beer")
@RestController
public class BeerController {

  public static Integer DEFAULT_RESULTS_PER_PAGE = 25;
  private final BeerService beerService;

  @GetMapping
  public ResponseEntity<BeerPagedList> getBeers(@RequestParam(required = false) Integer page,
                                                @RequestParam(required = false) Integer size,
                                                @RequestParam(required = false) String beerName,
                                                @RequestParam(required = false)BeerStyleEnum beerStyle) {
    if (page == null || page < 0) page = 0;
    if (size == null || size < 1) size = DEFAULT_RESULTS_PER_PAGE;

    BeerPagedList beerPagedList = beerService.listBeers(beerName, beerStyle, PageRequest.of(page, size));

    return ResponseEntity.ok(beerPagedList);
  }

  @GetMapping("/{beerId}")
  public ResponseEntity<BeerDto> getBeerById(@PathVariable("beerId") UUID beerId) {
    return new ResponseEntity<>(beerService.getById(beerId), HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<BeerDto> saveNewBeer(@RequestBody @Validated BeerDto beerDto) {
    return new ResponseEntity<>(beerService.saveNewBeer(beerDto), HttpStatus.CREATED);
  }

  @PutMapping("/{beerId}")
  public ResponseEntity<BeerDto> updateBeerById(@PathVariable("beerId") UUID beerId,
                                          @RequestBody @Validated BeerDto beerDto) {
    return new ResponseEntity<>(beerService.updateBeer(beerId, beerDto), HttpStatus.NO_CONTENT);
  }

}
