package tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Expansion {
  /**
   * Schema and version
   */
  private String schema;
  /**
   * New, three digit id. This is immutable.
   */
  private String id;
  /**
   * Full Expansion Name (e.g. Base Expansion)
   */
  private String name;
  /**
   * url compatible id (e.g. base-expansion)
   */
  private String seoName;
  /**
   * Enum id (core id) is used by game engine and card implementations
   */
  private String enumId;
  /**
   * Abbreviation. i.e. ptcgo code
   */
  private String abbr;
  private String pioId;

  // respective to all sets
  private Integer order;
  private List<String> categories;
  private String series;
  private Integer officialCount;
  private String releaseDate;
  private String imageUrl;
  private String symbolUrl;
  /**
   * If the entire expansion is not implemented yet, put this flag up
   */
  private Boolean notImplemented;
  /**
   * all cards of this expansion, populated at runtime
   */
  private List<Card> cards;
  /**
   * all formats that this expansion is allowed in, including partial expansions, populated at runtime
   */
  private List<Format> formats;
  private String filename;

  public void copyStaticPropertiesTo(Expansion other){
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
