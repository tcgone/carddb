package tcgone.carddb.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

public class Set {
  /**
   * New, three digit id. This is immutable.
   */
  public String id;
  /**
   * Full Set Name (e.g. Base Set)
   */
  public String name;
  /**
   * url compatible id (e.g. base-set)
   */
  public String seoName;
  /**
   * Enum id (core id) is used by game engine and card implementations
   */
  public String enumId;
  /**
   * Abbreviation. i.e. ptcgo code
   */
  public String abbr;
  public String pioId;

  // respective to all sets
  public Integer order;
  public List<String> categories;
  public String series;
  public Integer officialCount;
  public String releaseDate;
  public String imageUrl;
  public String symbolUrl;
  /**
   * If the entire set is not implemented yet, put this flag up
   */
  public boolean notImplemented;
  /**
   * all cards of this set, populated at runtime
   */
  public List<Card> cards;
  /**
   * all formats that this set is allowed in, including partial sets, populated at runtime
   */
  public List<Format> formats;

  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
