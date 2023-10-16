package tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Card {
  /**
   * id. e.g. 101-4
   */
  private String id;
  /**
   * Pio id. e.g. base1-4
   */
  private String pioId;
  /**
   * Engine id. e.g. CHARIZARD_4
   */
  private String enumId;
  /**
   * (DERIVED FIELD, DO NOT FILL)
   */
  private Expansion expansion;
  /**
   * Name of the card. e.g. Charizard
   */
  private String name;
  /**
   * e.g. 4
   */
  private Integer nationalPokedexNumber;
  /**
   * e.g. 4
   */
  private String number;
  /**
   * Medium image url: https://tcgone.net/scans/m/base_set/4.jpg
   */
  private String imageUrl;
  /**
   * Large image url: https://tcgone.net/scans/l/base_set/4.jpg
   */
  private String imageUrlHiRes;
  /**
   * Array of types (i.e. colors). e.g. ["R"]
   *
   * Logically it makes more sense to name this field 'colors', but precisely
   * speaking, it's used as 'type' everywhere else. https://bulbapedia.bulbagarden.net/wiki/Type_(TCG)
   */
  private List<Type> types;
  /**
   * Either Pokémon, Trainer or Energy
   */
  private CardType superType;
  /**
   * e.g. [EVOLUTION, STAGE2]
   */
  private List<CardType> subTypes;
  /**
   * (DERIVED FIELD, DO NOT FILL)
   * Evolution stage of the Pokemon. i.e. STAGE2.
   * Is null for non-pokemon cards.
   * Is automatically calculated and validated from {@link #subTypes} property.
   */
  private CardType stage;
  /**
   * Charmeleon
   */
  private String evolvesFrom;
  /**
   *
   */
  private List<String> evolvesTo;
  /**
   * 120
   */
  private Integer hp;
  /**
   * 3
   */
  private Integer retreatCost;
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
   * (DERIVED FIELD, DO NOT FILL) e.g. Charizard (BS 4)
   */
  private String fullName;
  /**
   * (DERIVED FIELD, DO NOT FILL)
   */
  private String seoName;
  /**
   * Rare Holo
   */
  private Rarity rarity;
  /**
   * Epic
   */
  private CareerRarity careerRarity;
  /**
   * Trainer/Energy text/Pokemon ruling text. Each entry is a line.
   */
  private List<String> text;
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
   * e.g. D
   */
  private String regulationMark;
  /**
   * (Unused ATM) List of erratas that apply to this card. It includes engine-level erratas.
   */
  private List<String> erratas;
//  /**
//   * {@code pokemonPower { def set = [] as Set def eff1, eff2 onActivate { if(eff1) eff1.unregister() if(eff2)
//   * eff2.unregister() eff1 = delayed { before BETWEEN_TURNS, { set.clear() } } eff2 = getter GET_ENERGY_TYPES, {
//   * holder-> if(set.contains(holder.effect.card)) { int count = holder.object.size() holder.object =
//   * [(1..count).collect{[FIRE] as Set}] } } } actionA { assert !(self.specialConditions) : "$self is affected by a
//   * special condition" def newSet = [] as Set newSet.addAll(self.cards.filterByType(ENERGY)) if(newSet != set){
//   * powerUsed() set.clear() set.addAll(newSet) } else { wcu "Nothing to burn more" } } } move { onAttack { damage 100
//   * discardSelfEnergy(C) // one energy card discardSelfEnergy(C) // one energy card } }}
//   */
//  public String script;

  /**
   * Id of the main variant. You may leave it empty to assume a variant on its own by its {@link #id}. Example: 101-12
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
   * (DERIVED FIELD, DO NOT FILL) list of variants.
   */
  private List<Variant> variants;
  /**
   * (DERIVED FIELD, DO NOT FILL) effective card implementation copy target.
   */
  private String copyOf;

  /**
   * true when this has been merged with pio, so the definition is finalized. merged cards won't be attempted to be
   * merged again, so the process can be restarted when failed.
   */
  private Boolean merged;
  /**
   * Sort order (respective to its expansion)
   */
  private Integer order;
  /**
   * (DERIVED FIELD, DO NOT FILL) Legal format seoNames
   */
  private List<String> formats;
  /**
   * (DERIVED FIELD, DO NOT FILL) full plain format text. e.g. Charizard (BS 4)
   */
  private String fullText;
  /**
   * (DERIVED FIELD, DO NOT FILL) full seo title
   */
  private String seoTitle;

//  public void copyStaticPropertiesTo(Card other){
//    other.name=this.name;
//    other.subTypes=this.subTypes;
//    other.superType=this.superType;
//    other.imageUrl=this.imageUrl;
//    other.types=this.types;
//    other.stage=this.stage;
//    other.hp=this.hp;
//    other.retreatCost=this.retreatCost;
//    other.abilities=this.abilities;
//    other.moves=this.moves;
//    other.weaknesses=this.weaknesses;
//    other.resistances=this.resistances;
//    other.evolvesFrom=this.evolvesFrom;
//    other.evolvesTo=this.evolvesTo;
//    other.nationalPokedexNumber=this.nationalPokedexNumber;
//    other.energy=this.energy;
//    other.text=this.text;
//  }


  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Card card = (Card) o;
    return Objects.equals(id, card.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

}
