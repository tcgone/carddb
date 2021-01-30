package tcgone.carddb.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Experimental
 *
 * @author axpendix@hotmail.com
 */
public enum CardType {

  POKEMON(10, "Pokémon", "pokemon", "pokémon"),
  ENERGY(20, "Energy", "energy"),
  TRAINER(30, "Trainer", "trainer"),

  MEGA_POKEMON(-6, "MEGA", "mega"),
  LVL_X(-5, "LV.X", "lvx", "levelup"),
  BREAK(-4, "BREAK", "break"),
  STAGE2(-2, "Stage 2", "stage2", "s2"),
  STAGE1(-1, "Stage 1", "stage1", "s1"),
  EVOLUTION(0, "Evolution", "evolution"),
  RESTORED(4, "Restored", "restored"),
  BASIC(7, "Basic", "basic"),
  BABY(8, "Baby", "baby"),
  LEGEND(9, "LEGEND", "legend"),

  BASIC_ENERGY(18, "Basic Energy", "basic-energy", "basic"),
  SPECIAL_ENERGY(19, "Special Energy", "special-energy", "special"),
  ITEM(23, "Item", "item"),
  SUPPORTER(24, "Supporter", "supporter"),
  STADIUM(25, "Stadium", "stadium"),
  POKEMON_TOOL(26, "Pokémon Tool", "tool"),
  TECHNICAL_MACHINE(27, "Technical Machine", "technical-machine", "tm"),
  FLARE(28, "Team Flare Hyper Gear", "flare"),
  ROCKETS_SECRET_MACHINE(29, "Rocket's Secret Machine", "rockets-secret-machine"),

  TAG_TEAM(93, "TAG TEAM", "tag-team"),
  ULTRA_BEAST(94, "Ultra Beast", "ultra-beast"),
  PRISM_STAR(95, "Prism Star", "prism-star"),
  POKEMON_GX(96, "Pokémon-GX", "pokemon-gx", "gx"),
  POKEMON_PRIME(97, "Pokémon Prime", "pokemon-prime", "prime"),
  POKEMON_STAR(98, "Pokémon Star", "pokemon-star", "star"),
  POKEMON_EX(99, "Pokémon-EX", "pokemon-ex", "ex"), //UPPERCASE
  EX(100, "Pokémon-ex", "legacy-ex", "ex"), //LOWERCASE
  FOSSIL(101, "Fossil", "fossil"),
  TEAM_MAGMA(102, "Team Magma", "team-magma", "magma"),
  TEAM_AQUA(103, "Team Aqua", "team-aqua", "aqua"),
  OWNERS_POKEMON(104, "Owner's", "owners"),
  DARK_POKEMON(105, "Dark", "dark"),
  LIGHT_POKEMON(106, "Light", "light"),
  SHINING_POKEMON(107, "Shining", "shining"),
  TEAM_PLASMA(108, "Team Plasma", "team-plasma", "plasma"),
  ACE_SPEC(109, "ACE Spec", "ace-spec"),
  HAS_ANCIENT_TRAIT(110, "Has Ancient Trait", "has-ancient-trait"),
  G_SPEC(111, "G-SPEC", "g-spec"),
  POKEMON_V(112, "Pokémon V", "pokemon-v"),
  VMAX(113, "Pokémon VMAX", "vmax"),

  NOT_IMPLEMENTED(201, "Not Implemented", "not-implemented"),

  ;

  private final int priority;
  private final List<String> searchLabels;
  private final String displayLabel;

  CardType(int priority, String displayLabel, String... searchLabels) {
    this.priority = priority;
    this.displayLabel = displayLabel;
    this.searchLabels = Arrays.asList(searchLabels);
  }

  public int getPriority() {
    return priority;
  }

  public String getDisplayLabel() {
    return displayLabel;
  }

  public List<String> getSearchLabels() {
    return searchLabels;
  }

  public static class CardTypeComparator implements Comparator<CardType>, Serializable {
    @Override
    public int compare(CardType o1, CardType o2) {
      return Integer.compare(o1.priority, o2.priority);
    }
  }

  public boolean isSuperType() {
    return this == POKEMON || this == TRAINER || this == ENERGY;
  }

}
