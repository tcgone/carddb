package net.tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

public class Format {
  @NotBlank
  public String name;
  @NotBlank
  public String seoName;
  @NotBlank
  public String enumId;
  @NotBlank
  public String description;
  public String imageUrl;
  /**
   * list of {@link Set#id}s
   *
   * if a set is specified inside format.sets, then it is displayed as part of that format.
   * note that this is just a display. the actual card list of a format can be different than this.
   * example: promo sets may not be mentioned inside sets clause, but included in the "includes" field.
   */
  @Size(min = 1)
  @JsonProperty("sets")
  public List<String> sets;
  /**
   * list of {@link Card#id}s that were specifically included in this format
   *
   * if one card from a set is specified in includes field, it is assumed everything else in the same set is excluded.
   * it is a violation to both specify includes and excludes for the same set.
   */
  public List<String> includes;
  /**
   * list of {@link Card#id}s that were excluded from this format
   *
   * if one card from a set is specified in excludes field, it is assumed everything else in the same set is included.
   * it is a violation to both specify includes and excludes for the same set.
   */
  public List<String> excludes;
  @NotBlank
  public String ruleSet;
  public int order;
  public List<String> flags;
//  private List<String> availableGameTypes;
//  private String playUrl;//can be per game type
}
