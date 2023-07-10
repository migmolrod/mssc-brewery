package guru.sfg.mssctaskservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PingMessage implements Serializable {

  static final long serialVersionUID = 152794090814236915L;

  private UUID id;
  private String message;
}
