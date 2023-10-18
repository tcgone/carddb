package tcgone.carddb.tools;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import tcgone.carddb.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;

/**
 * @author axpendix@hotmail.com
 */
public class ImplTmplGenerator {

  public void writeAll(Collection<ExpansionFile> expansionFiles) throws Exception {
    new File("impl").mkdirs();
    for (ExpansionFile expansionFile : expansionFiles) {
      write(expansionFile);
    }
  }
  private void write(ExpansionFile expansionFile) throws Exception {
    /*
     * expansion.vm requires:
     * classname foldername collection
     * list1: name (enum const name), fullname, cardtype (enum const)
     * rarity (enum const), cln (collection line no)
     * list2: name (enum const name), impl (new x())
     */
    Map<String, Object> modelmap = new HashMap<>();
    List<List1Item> list1 = new ArrayList<>();
    List<List2Item> list2 = new ArrayList<>();
    String EXPNNAME= expansionFile.getExpansion().getEnumId();
    modelmap.put("classname", WordUtils.capitalizeFully(EXPNNAME.replaceAll("_"," ")).replaceAll(" ",""));
    modelmap.put("foldername", EXPNNAME.toLowerCase(Locale.ENGLISH));
    modelmap.put("collection", EXPNNAME);
    modelmap.put("list1", list1);
    modelmap.put("list2", list2);

    // the magic happens here
    for (Card card : expansionFile.getCards()) {
      List<String> cardTypeSet = new ArrayList<>();
      String rarity = null;
      String rc = String.valueOf(card.getRetreatCost());
      String hp = null;
      String predecessor = null;
      String cardtext = null;
      if (card.getText() != null) {
        cardtext = StringUtils.join(card.getText(), "\" +\n\t\t\t\t\t\"");
      }
      String typesCombined = null;
      StringBuilder weakness = new StringBuilder();
      StringBuilder resistance = new StringBuilder();
      StringBuilder moves = new StringBuilder();
      StringBuilder abilities = new StringBuilder();

      // rarity
      if (card.getRarity() == Rarity.RARE_HOLO) {
        rarity = "HOLORARE";
      } else if (card.getRarity() == Rarity.ULTRA_RARE) {
        rarity = "ULTRARARE";
      } else {
        rarity = card.getRarity().name();
      }
      for (CardType subType : card.getCardTypes()) {
        cardTypeSet.add(subType.name());
      }
      if (card.getCardTypes().contains(CardType.POKEMON)) {
        //hp
        hp = String.valueOf(card.getHp());
        if (hp.length() < 3) {
          hp = "0" + hp;
        }
        hp = String.format("HP%s", hp);
        //types
        if (card.getTypes() != null) {
          if (card.getTypes().size() == 1) {
            typesCombined = card.getTypes().get(0).getNotation();
          } else {
            typesCombined = card.getTypes().toString();
          }
          for (Type _type : card.getTypes()) {
            cardTypeSet.add("_" + _type.name() + "_");
          }
        }
        if (card.getCardTypes().contains(CardType.BABY)) {
          cardTypeSet.add("BASIC");
        }
        predecessor = (card.getEvolvesFrom() != null && !card.getEvolvesFrom().isEmpty()) ? card.getEvolvesFrom().get(0) : null;
        if (card.getWeaknesses() != null) {
          for (WeaknessResistance wr : card.getWeaknesses()) {
            weakness.append(String.format("weakness %s%s\n\t\t\t\t", wr.getType().getNotation(), !wr.getValue().equalsIgnoreCase("x2") ? ", '" + wr.getValue() + "'" : ""));
          }
        }
        if (card.getResistances() != null) {
          for (WeaknessResistance wr : card.getResistances()) {
            String typ = "";
            if ("-20".equals(wr.getValue())) typ = ", MINUS20";
            if ("-30".equals(wr.getValue())) typ = ", MINUS30";
            resistance.append(String.format("resistance %s\n\t\t\t\t", wr.getType().getNotation() + typ));
          }
        }
        if (card.getAbilities() != null) {
          for (Ability a : card.getAbilities()) {
            if (a.getType().equalsIgnoreCase("Pokémon Power")) {
              abilities.append(String.format("pokemonPower \"%s\", {\n" +
                "\t\t\t\t\ttext \"%s\"\n" +
                "\t\t\t\t\tactionA {\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n\t\t\t\t", a.getName(), a.getText()));
            }
            if (a.getType().equalsIgnoreCase("Poké-Power")) {
              abilities.append(String.format("pokePower \"%s\", {\n" +
                "\t\t\t\t\ttext \"%s\"\n" +
                "\t\t\t\t\tactionA {\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n\t\t\t\t", a.getName(), a.getText()));
            }
            if (a.getType().equalsIgnoreCase("Poké-Body")) {
              abilities.append(String.format("pokeBody \"%s\", {\n" +
                "\t\t\t\t\ttext \"%s\"\n" +
                "\t\t\t\t\tdelayedA {\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n\t\t\t\t", a.getName(), a.getText()));
            }
            if (a.getType().equalsIgnoreCase("Ability")) {
              abilities.append(String.format("bwAbility \"%s\", {\n" +
                "\t\t\t\t\ttext \"%s\"\n" +
                "\t\t\t\t\tactionA {\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n\t\t\t\t", a.getName(), a.getText()));
            }
            if (a.getType().equalsIgnoreCase("Ancient Trait") || a.getName().startsWith("Ω") || a.getName().startsWith("α") || a.getName().startsWith("Δ") || a.getName().startsWith("θ")) {
              abilities.append(String.format("ancientTrait \"%s\", {\n" +
                "\t\t\t\t\ttext \"%s\"\n" +
                "\t\t\t\t\tdelayedA {\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n\t\t\t\t", a.getName(), a.getText()));
            }
          }
        }
        if (card.getMoves() != null) {
          for (Move m : card.getMoves()) {
            String movedesc = "";
            String movedamg = null;
            if (m.getDamage() != null) {
              movedesc += m.getDamage() + " damage. ";
              movedamg = m.getDamage().replaceAll("[^\\d]", "");
            }
            if (m.getText() != null)
              movedesc += m.getText();

            String trailingString = "\n\t\t\t\t";
            if (card.getMoves().indexOf(m) == (card.getMoves().size() - 1)) {
              trailingString = "";
            }
            moves.append(String.format("move \"%s\", {\n" +
              "\t\t\t\t\ttext \"%s\"\n" +
              "\t\t\t\t\tenergyCost %s\n" +
              "\t\t\t\t\tattackRequirement {}\n" +
              "\t\t\t\t\tonAttack {\n" +
              "\t\t\t\t\t\t%s\n" +
              "\t\t\t\t\t}\n" +
              "\t\t\t\t}%s", m.getName(), movedesc, StringUtils.join(m.getCost(), ", "), movedamg != null ? "damage " + movedamg : "", trailingString));

          }
        }
      }

      String impl = null;
      if (cardTypeSet.contains("BABY")) {
        impl = String.format("baby (this, successors:%s, hp:%s, type:%s, retreatCost:%s) {\n" +
            "\t\t\t\t%s%s%s%s\n" +
            "\t\t\t}",
          "'SUCCESSOR(S)'", hp, typesCombined, rc, weakness, resistance, abilities, moves);
      } else if (cardTypeSet.contains("BASIC") || cardTypeSet.contains("RESTORED")) {
        impl = String.format("basic (this, hp:%s, type:%s, retreatCost:%s) {\n" +
            "\t\t\t\t%s%s%s%s\n" +
            "\t\t\t}",
          hp, typesCombined, rc, weakness, resistance, abilities, moves);
      } else if (cardTypeSet.contains("EVOLUTION") || cardTypeSet.contains("VMAX") || cardTypeSet.contains("VSTAR")) {
        impl = String.format("evolution (this, from:\"%s\", hp:%s, type:%s, retreatCost:%s) {\n" +
            "\t\t\t\t%s%s%s%s\n" +
            "\t\t\t}",
          predecessor, hp, typesCombined, rc, weakness, resistance, abilities, moves);
      } else if (cardTypeSet.contains("SUPPORTER")) {
        impl = String.format("supporter (this) {\n" +
          "\t\t\t\ttext \"%s\"\n" +
          "\t\t\t\tonPlay {\n" +
          "\t\t\t\t}\n" +
          "\t\t\t\tplayRequirement{\n" +
          "\t\t\t\t}\n" +
          "\t\t\t}", cardtext);
      } else if (cardTypeSet.contains("STADIUM")) {
        impl = String.format("stadium (this) {\n" +
          "\t\t\t\ttext \"%s\"\n" +
          "\t\t\t\tonPlay {\n" +
          "\t\t\t\t}\n" +
          "\t\t\t\tonRemoveFromPlay{\n" +
          "\t\t\t\t}\n" +
          "\t\t\t}", cardtext);
      } else if (cardTypeSet.contains("POKEMON_TOOL")) {
        impl = String.format("pokemonTool (this) {\n" +
          "\t\t\t\ttext \"%s\"\n" +
          "\t\t\t\tonPlay {reason->\n" +
          "\t\t\t\t}\n" +
          "\t\t\t\tonRemoveFromPlay {\n" +
          "\t\t\t\t}\n" +
          "\t\t\t\tallowAttach {to->\n" +
          "\t\t\t\t}\n" +
          "\t\t\t}", cardtext);
      } else if (cardTypeSet.contains("ITEM")) {
        impl = String.format("itemCard (this) {\n" +
          "\t\t\t\ttext \"%s\"\n" +
          "\t\t\t\tonPlay {\n" +
          "\t\t\t\t}\n" +
          "\t\t\t\tplayRequirement{\n" +
          "\t\t\t\t}\n" +
          "\t\t\t}", cardtext);
      } else if (cardTypeSet.contains("TRAINER")) {
        impl = String.format("basicTrainer (this) {\n" +
          "\t\t\t\ttext \"%s\"\n" +
          "\t\t\t\tonPlay {\n" +
          "\t\t\t\t}\n" +
          "\t\t\t\tplayRequirement{\n" +
          "\t\t\t\t}\n" +
          "\t\t\t}", cardtext);
      } else if (cardTypeSet.contains("SPECIAL_ENERGY")) {
        impl = String.format("specialEnergy (this, [[C]]) {\n" +
          "\t\t\t\ttext \"%s\"\n" +
          "\t\t\t\tonPlay {reason->\n" +
          "\t\t\t\t}\n" +
          "\t\t\t\tonRemoveFromPlay {\n" +
          "\t\t\t\t}\n" +
          "\t\t\t\tonMove {to->\n" +
          "\t\t\t\t}\n" +
          "\t\t\t\tallowAttach {to->\n" +
          "\t\t\t\t}\n" +
          "\t\t\t}", cardtext);
      } else if (cardTypeSet.contains("BASIC_ENERGY")) {
        impl = String.format("basicEnergy (this, %s)", card.getEnergy().get(0).get(0).getNotation());
      }
      if (impl == null) {
        throw new IllegalStateException("Impl null:" + card.getName() + "," + card.getNumber());
      }

      if (card.getVariantOf() != null) {
        //search for reprints in same expansion
        for (List2Item list2Item : list2) {
          if (list2Item.getId().equals(card.getVariantOf())) {
            impl = String.format("copy (%s, this)", list2Item.name);
            System.out.println("REPRINT_SAME " + list2Item.name);
            break;
          }
        }
      }

      list1.add(new List1Item(card.getEnumId(), card.getName(), cardTypeSet, rarity, card.getNumber()));
      list2.add(new List2Item(card.getEnumId(), card.getEnumId(), impl));
    }

    try {
      Properties properties = new Properties();
      properties.setProperty("resource.loader", "class");
      properties.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
      properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
      Velocity.init(properties);
      VelocityContext context = new VelocityContext(modelmap);
      Writer writer = new FileWriter(new File(String.format("impl/%s.groovy", modelmap.get("classname"))));
      Velocity.mergeTemplate("set2.vm", "UTF-8", context, writer);
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Value
  public static class List1Item {
    String name;
    String fullname;
    List<String> cardTypes;
    String rarity;
    String cardNumber;
  }

  @Value
  public static class List2Item {
    String id;
    String name;
    String impl;
  }
}
