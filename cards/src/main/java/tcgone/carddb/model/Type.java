package tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * @author axpendix@hotmail.com
 */
public enum Type {

  GRASS("G"),
  FIRE("R"),
  WATER("W"),
  LIGHTNING("L"),
  PSYCHIC("P"),
  FIGHTING("F"),
  DARKNESS("D"),
  METAL("M"),
  FAIRY("Y"),
  DRAGON("N"),
  COLORLESS("C"),

  RAINBOW("RAINBOW"),
  MAGMA("MAGMA"),
  AQUA("AQUA");

//  @JsonValue //
  @Getter
  private final String notation;
  private final String label;

  Type(String notation) {
    this.notation = notation;
    this.label = StringUtils.capitalize(notation.toLowerCase(Locale.ENGLISH));
  }

  public String getEnclosedNotation() {
    return "[" + notation + "]";
  }

  @JsonCreator
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static Type of(String input) {
    if (input.equals("[-]")) return null; // PIO uses [-] for costless moves instead of an empty list
    for (Type value : values()) {
      if (value.notation.equalsIgnoreCase(input) || value.name().equalsIgnoreCase(input) || value.label.equalsIgnoreCase(input))
        return value;
    }
    throw new IllegalArgumentException("Type for '" + input + "' was not found.");
  }

//  public static List<Type> valuesForPokemon() {
//    return Arrays.asList(GRASS, FIRE, WATER, LIGHTNING, PSYCHIC, FIGHTING, DARKNESS, METAL, FAIRY, DRAGON, COLORLESS);
//  }

}
