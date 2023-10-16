package tcgone.carddb.model;

import lombok.Data;

import java.util.List;

@Data
public class Format {
  private String name;
  private String seoName;
  private String enumId;
  private String description;
  private String imageUrl;
  /**
   * list of {@link Expansion#getId}s
   *
   * if a expansion is specified inside format.sets, then it is displayed as part of that format.
   * note that this is just a display. the actual card list of a format can be different then this.
   * example: promo sets may not be mentioned inside sets clause, but included in the "includes" field.
   */
  private List<String> expansions;
  /**
   * list of {@link Card#getId()}s that were specifically included in this format
   * if one card from an expansion is specified in includes field, it is assumed everything else in the same expansion is excluded.
   * it is a violation to both specify includes and excludes for the same expansion.
   */
  private List<String> includes;
  /**
   * list of {@link Card#getId}s that were excluded from this format
   * if one card from an expansion is specified in excludes field, it is assumed everything else in the same expansion is included.
   * it is a violation to both specify includes and excludes for the same expansion.
   */
  private List<String> excludes;
  private String ruleSet;
  private Integer order;
  private List<String> flags;
//  private List<String> availableGameTypes;
//  private String playUrl;//can be per game type
}
