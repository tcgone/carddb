package tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.text.WordUtils;

public enum VariantType {

  REGULAR,//(assumed for those which does not specify this field but override is possible)
  REPRINT,//(reprints in later expansions),default if there is no variantType with a variantId.
  HOLO,//(regular holo)
  REVERSE,//(reverse holo)
  ALTERNATE_ART,
  FULL_ART,
  SECRET_ART,
  PROMO;

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
