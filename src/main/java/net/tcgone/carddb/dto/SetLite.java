package net.tcgone.carddb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetLite {
  private String id;
  private String name;
  private String abbr;
}
