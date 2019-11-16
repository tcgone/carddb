package net.tcgone.carddb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardLite {
  private String id;
  private String name;
  private String imageUrl;
  private Integer quantity; //for inventory & deck
}
