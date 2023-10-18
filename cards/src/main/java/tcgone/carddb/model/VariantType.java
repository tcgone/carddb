package tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.text.WordUtils;

public enum VariantType {

  /**
   * assumed for those which does not specify this field but override is possible
   */
  REGULAR,
  /**
   * (reprints in later expansions),default if there is no variantType with a variantId != enumId.
   */
  REPRINT,
  /**
   * holo rare version
   */
  HOLO,
  /**
   * alternate art version
   */
  ALTERNATE_ART,
  /**
   * full art
   */
  FULL_ART,
  /**
   * secret art
   */
  SECRET_ART,
  /**
   * promo
   */
  PROMO,
  /**
   * for fan-made art versions
   */
  FAN_ART,
  ;

  private final String label;

  VariantType() {
    this.label = WordUtils.capitalizeFully(name().replace("_"," "));
  }

  @JsonValue
  public String getLabel() {
    return label;
  }

  @JsonCreator
  public static VariantType of(String input){
    for (VariantType value : values()) {
      if(value.name().equals(input) || value.label.equals(input))
        return value;
    }
    throw new IllegalStateException("No VariantType for "+input);
  }



}
