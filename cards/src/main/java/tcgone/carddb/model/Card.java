package tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode(of = {"enumId"})
public class Card {
  /**
   * Engine with expansion. e.g. CHARIZARD_4:BASE_SET
   */
  private String enumId;
  /**
   * {@link #enumId} of the main variant. If empty, it is regarded as a variant on its own.
   */
  private String variantOf;
  /**
   * How did this card get copied. Example, if THIS CARD is Holo version of 101-12, put Holo here.
   */
  private VariantType variantType;
  /**
   * True when the variant has any altering ruling, text or type change.
   */
  private Boolean variantIsDifferent;
  /**
   * e.g. BASE_SET
   */
  private String expansionEnumId;
  /**
   * Name of the card. e.g. Charizard
   */
  private String name;
  /**
   * e.g. 4
   */
  private String number;
  /**
   * e.g. [POKEMON, EVOLUTION, STAGE2]
   */
  private List<CardType> cardTypes;
  /**
   * Array of types (i.e. colors). e.g. ["R"]
   * Logically it makes more sense to name this field 'colors', but precisely
   * speaking, it's used as 'type' everywhere else. <a href="https://bulbapedia.bulbagarden.net/wiki/Type_(TCG)">...</a>
   */
  private List<Type> types;
  /**
   * Charmeleon
   */
  private List<String> evolvesFrom;
  /**
   * 120
   */
  private Integer hp;
  /**
   * [ { "type": "Pokémon Power", "name": "Energy Burn", "text": "As often as you like during your turn (before your
   * attack), you may turn all Energy attached to Charizard into [R] for the rest of the turn. This power can't be used
   * if Charizard is Asleep, Confused, or Paralyzed." } ]
   */
  private List<Ability> abilities;
  /**
   * [ { "cost": ["R","R","R","R"], "name": "Fire Spin", "text": "Discard 2 Energy cards attached to Charizard in order
   * to use this attack.", "damage": "100", "convertedEnergyCost": 4 } ]
   */
  private List<Move> moves;
  /**
   * [ { "type": "W", "value": "×2" } ]
   */
  private List<WeaknessResistance> weaknesses;
  /**
   * [ { "type": "F", "value": "-30" } ]
   */
  private List<WeaknessResistance> resistances;
  /**
   * 3
   */
  private Integer retreatCost;
  /**
   * Rare Holo
   */
  private Rarity rarity;
  /**
   * Trainer/Energy text/Pokemon ruling text. Each entry is a line.
   */
  private String text;
  /**
   * (Energy only) Energy types
   */
  private List<List<Type>> energy;
  /**
   * e.g. Mitsuhiro Arita
   */
  private String artist;
  /**
   * e.g. Spits fire that is hot enough to melt boulders. Known to unintentionally cause forest fires.
   */
  private String flavorText;
  /**
   * pokemontcg.io id. e.g. base1-4
   */
  private String pioId;
  /**
   * e.g. 4
   */
  private Integer nationalPokedexNumber;
  /**
   * e.g. D
   */
  private String regulationMark;
  /**
   * List of erratas that apply to this card. It includes engine-level erratas.
   */
  private List<String> erratas;

  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

  /**
   * useful to detect reprints during variant handling
   */
  public String generateDiscriminatorFullText() {
//    return String.format("name=%s, cardTypes=%s, hp=%d, evolvesFrom=%s, types=%s, abilities=%s, moves=%s, weakness=%s, resistance=%s, retreatCost=%d, text=%s, energy=%s", name, cardTypes, hp, evolvesFrom, types, abilities, moves, weaknesses, resistances, retreatCost, text, energy);
    return String.format("name=%s, cardTypes=%s, hp=%d, evolvesFrom=%s, types=%s, abilities=%s, moves=%s, weakness=%s, resistance=%s, retreatCost=%d, text=%s, energy=%s", name, cardTypes, hp, evolvesFrom, types, abilities == null ? null : abilities.stream().map(ability -> String.format("%s: %s", ability.getType(), ability.getName())).collect(Collectors.toList()), moves == null ? null : moves.stream().map(move -> String.format("%s %s %s", move.getCost(), move.getName(), move.getDamage())).collect(Collectors.toList()), weaknesses, resistances, retreatCost, text, energy).toLowerCase();
  }
}
