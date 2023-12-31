package guru.sfg.brewery.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Created by jt on 2019-05-12.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class BeerDto implements Serializable {

  static final long serialVersionUID = 7974783006454456981L;

  @Null
  private UUID id;

  @Null
  private Integer version;

  @Null
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ", shape = JsonFormat.Shape.STRING)
  private OffsetDateTime createdDate;

  @Null
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ", shape = JsonFormat.Shape.STRING)
  private OffsetDateTime lastModifiedDate;

  @NotBlank
  private String beerName;

  @NotNull
  private String beerStyle;

  @NotNull
  private String upc;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  @Positive
  @NotNull
  private BigDecimal price;

  @Positive
  private Integer quantityOnHand;

  @Positive
  private Integer quantityToBrew;

}
