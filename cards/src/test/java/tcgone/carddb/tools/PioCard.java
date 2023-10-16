package tcgone.carddb.tools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tcgone.carddb.model.Ability;
import tcgone.carddb.model.Move;
import tcgone.carddb.model.Type;
import tcgone.carddb.model.WeaknessResistance;

import java.util.List;

/**
 * models pokemontcg.io card format
 * @author axpendix@hotmail.com
 */
@JsonIgnoreProperties(value = {"legalities", "images"})
public class PioCard {
  public String id;
  public String name;
  public String imageUrl;//optional
  public String imageUrlHiRes;//optional
  public List<String> subtypes;// pio v2 optional
  public String subtype;
  public String supertype;
  public String level;
  public String evolvesFrom;
  public List<String> evolvesTo;//optional
  public String hp; // trainers have 'None'
  public List<String> retreatCost;
  public Integer convertedRetreatCost;
  public String number;
  public String artist;//optional
  public String rarity;
  public String flavorText;// pio v2 optional
  public List<Integer> nationalPokedexNumbers;// pio v2 optional
  public String series;
  public String set;
  public String setCode;
  public List<Type> types;
  public List<String> rules;// pio v2 optional
  public List<Move> attacks;
  public Ability ability;
  public List<Ability> abilities;//pio v2 optional
  public Ability ancientTrait;
  public List<WeaknessResistance> weaknesses;
  public List<WeaknessResistance> resistances;
  public Integer nationalPokedexNumber;//optional
  public List<String> text; // for trainers, sp. energy
  public String regulationMark; // Modern method of determining Standard format legality

}
