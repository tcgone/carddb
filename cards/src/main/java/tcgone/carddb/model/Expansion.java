package tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Locale;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Expansion {
  /**
   * three digit order id, i.e. 111 for base set
   */
  private String orderId;
  /**
   * Full Expansion Name (e.g. Base Set)
   */
  private String name;
  /**
   * url compatible id (e.g. base-set)
   */
  private String seoName;
  /**
   * Enum id: is used by game engine and card implementations. i.e. BASE_SET
   */
  private String enumId;
  /**
   * Abbreviation. i.e. BS
   */
  private String shortName;
  /**
   * pokemontcg.io id
   */
  private String pioId;
  private String series;
  private Integer officialCount;
  private String releaseDate;
  private String imageUrl;
  private String symbolUrl;
  private Boolean isFanMade;
  private Boolean notImplemented;

  public String generateFileName() {
    return String.format("%s-%s", orderId, enumId.toLowerCase(Locale.ENGLISH));
  }

  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
