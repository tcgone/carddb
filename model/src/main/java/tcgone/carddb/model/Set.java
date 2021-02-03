package tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Set {
  /**
   * Schema and version
   */
  public String schema;
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
  public Boolean notImplemented;
  /**
   * all cards of this set, populated at runtime
   */
  public List<Card> cards;
  /**
   * all formats that this set is allowed in, including partial sets, populated at runtime
   */
  public List<Format> formats;
  public String filename;

  public void copyStaticPropertiesTo(Set other){
    other.id = this.id;
    other.abbr=this.abbr;
    other.officialCount=this.officialCount;
    other.name=this.name;
    other.enumId=this.enumId;
    other.filename=this.filename;
    other.schema=this.schema;
    other.categories=this.categories;
    other.imageUrl=this.imageUrl;
    other.order=this.order;
    other.pioId=this.pioId;
    other.releaseDate=this.releaseDate;
    other.seoName=this.seoName;
    other.series=this.series;
    other.symbolUrl=this.symbolUrl;
  }

  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
