package tcgone.carddb.tools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"legalities", "images"})
public class PioSet {
  public String id;
  public String name;
  public String series;
  public Integer printedTotal;
  public Integer total;
  public String ptcgoCode;
  public String releaseDate;
  public String updatedAt;
}
