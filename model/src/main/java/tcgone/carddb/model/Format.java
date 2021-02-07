package tcgone.carddb.model;

import java.util.List;

public class Format {
  public String name;
  public String seoName;
  public String enumId;
  public String description;
  public String imageUrl;
  /**
   * list of {@link Expansion#id}s
   *
   * if a expansion is specified inside format.sets, then it is displayed as part of that format.
   * note that this is just a display. the actual card list of a format can be different than this.
   * example: promo sets may not be mentioned inside sets clause, but included in the "includes" field.
   */
  public List<String> sets;
  /**
   * list of {@link Card#id}s that were specifically included in this format
   *
   * if one card from a expansion is specified in includes field, it is assumed everything else in the same expansion is excluded.
   * it is a violation to both specify includes and excludes for the same expansion.
   */
  public List<String> includes;
  /**
   * list of {@link Card#id}s that were excluded from this format
   *
   * if one card from a expansion is specified in excludes field, it is assumed everything else in the same expansion is included.
   * it is a violation to both specify includes and excludes for the same expansion.
   */
  public List<String> excludes;
  public String ruleSet;
  public Integer order;
  public List<String> flags;
//  private List<String> availableGameTypes;
//  private String playUrl;//can be per game type
}
