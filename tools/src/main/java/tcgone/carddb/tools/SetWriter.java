/*
Copyright 2018 axpendix@hotmail.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package tcgone.carddb.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.util.NodeStyleResolver;
import com.fasterxml.jackson.dataformat.yaml.util.StringQuotingChecker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import tcgone.carddb.model.Expansion;
import tcgone.carddb.model.*;
import tcgone.carddb.model.VariantType;
import tcgone.carddb.model3.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.*;


/**
 * @author axpendix@hotmail.com
 */
public class SetWriter {
  private final YAMLMapper mapper;

  public static class EqualityCard extends Card {

    public EqualityCard(Card card) {
      this.setName(card.getName());
      this.setTypes(card.getTypes());
      this.setSuperType(card.getSuperType());
      this.setSubTypes(card.getSubTypes());
      this.setEvolvesFrom(card.getEvolvesFrom());
      this.setHp(card.getHp());
      this.setRetreatCost(card.getRetreatCost());
      this.setAbilities(card.getAbilities());
      this.setMoves(card.getMoves());
      this.setWeaknesses(card.getWeaknesses());
      this.setResistances(card.getResistances());
      this.setText(card.getText());
      this.setEnergy(card.getEnergy());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      EqualityCard card = (EqualityCard) o;
      return Objects.equals(getName(), card.getName()) &&
        Objects.equals(getTypes(), card.getTypes()) &&
        Objects.equals(getSuperType(), card.getSuperType()) &&
        Objects.equals(getSubTypes(), card.getSubTypes()) &&
        Objects.equals(getEvolvesFrom(), card.getEvolvesFrom()) &&
        Objects.equals(getHp(), card.getHp()) &&
        Objects.equals(getRetreatCost(), card.getRetreatCost()) &&
        Objects.equals(getAbilities(), card.getAbilities()) &&
        Objects.equals(getMoves(), card.getMoves()) &&
        Objects.equals(getWeaknesses(), card.getWeaknesses()) &&
        Objects.equals(getResistances(), card.getResistances()) &&
        Objects.equals(getText(), card.getText()) &&
        Objects.equals(getEnergy(), card.getEnergy());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getName(), getTypes(), getSuperType(), getSubTypes(), getEvolvesFrom(), getHp(), getRetreatCost(), getAbilities(), getMoves(), getWeaknesses(), getResistances(), getText(), getEnergy());
    }
  }

  SetWriter() {

    mapper = YAMLMapper.builder(
      YAMLFactory.builder()
        .nodeStyleResolver(s -> ("cost".equals(s)||"types".equals(s)||"subTypes".equals(s)||"evolvesTo".equals(s)||"energy".equals(s)) ? NodeStyleResolver.NodeStyle.FLOW : null)
        .stringQuotingChecker(new StringQuotingChecker.Default() {
          @Override
          public boolean needToQuoteValue(String s) {
            // https://yaml.org/spec/1.1/#plain%20style/syntax
            if (s != null && !s.contains(": ") && !s.contains(" #") && !s.contains("\t")
              && !s.startsWith("- ") && !s.startsWith("? ") && !s.startsWith(":"))
              return false;
            else
              return super.needToQuoteValue(s);
          }
        })
        .build()
    )
      .enable(ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
      .enable(MINIMIZE_QUOTES)
      .enable(USE_SINGLE_QUOTES)
      .disable(WRITE_DOC_START_MARKER)
      .nodeFactory(new SetWriter.SortingNodeFactory())
      .build();
//    objectMapper = new ObjectMapper(new YAMLFactory()
//      .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
//    );


  }
  static class SortingNodeFactory extends JsonNodeFactory {
    @Override
    public ObjectNode objectNode() {
      return new ObjectNode(this, new TreeMap<String, JsonNode>());
    }
  }
  public void writeAllE2(Collection<Expansion> expansions, String outputDirectory) throws IOException {
    new File(outputDirectory).mkdirs();
    for (Expansion expansion : expansions) {
      expansion.setFilename(String.format(outputDirectory+File.separator+"%s-%s.yaml", expansion.getId(), expansion.getEnumId().toLowerCase(Locale.ENGLISH)));
      for (Card card : expansion.getCards()) {
        card.setExpansion(null);
        card.setMerged(null);
        card.setFormats(null);
        if (card.getMoves() != null) {
          for (Move move : card.getMoves()) {
            move.setDamage(StringUtils.trimToNull(move.getDamage()));
            move.setText(StringUtils.trimToNull(move.getText()));
          }
        }
      }
      BufferedWriter out = new BufferedWriter
        (new OutputStreamWriter(Files.newOutputStream(Paths.get(expansion.getFilename())), StandardCharsets.UTF_8));
      expansion.setFilename(null);
      mapper.writeValue(out, expansion);
//      String dump = yaml.dumpAs(expansion, Tag.MAP, null);
//      out.write(dump);
      out.close();
    }
  }
  public List<ExpansionFile3> convertFromE2ToE3(Collection<Expansion> expansions){
    List<ExpansionFile3> result = new ArrayList<>();
    for (Expansion expansion : expansions) {
      Expansion3 expansion3 = new Expansion3();
      BeanUtils.copyProperties(expansion, expansion3);
      expansion3.setOrderId(expansion.getId());
      if (expansion3.getEnumId() == null) {
        throw new IllegalStateException(expansion3 + " has null enumId");
      }
      expansion3.setShortName(expansion.getAbbr());
      List<Card3> cards = new ArrayList<>();
      for (Card card : expansion.getCards()) {
        Card3 card3 = new Card3();
        BeanUtils.copyProperties(card, card3, "evolvesFrom", "text");
        if (card.getEvolvesFrom() != null){
          card3.setEvolvesFrom(Collections.singletonList(card.getEvolvesFrom()));
        }
        if (card.getText() != null && !card.getText().isEmpty() ) {
          card3.setText(String.join("\n", card.getText()));
        }
        card3.setExpansionEnumId(expansion3.getEnumId());
        card3.setCardTypes(new ArrayList<>());
        card3.getCardTypes().add(card.getSuperType());
        if (card.getSubTypes() != null)
          card3.getCardTypes().addAll(card.getSubTypes());
        cards.add(card3);
      }
      result.add(new ExpansionFile3("E3", expansion3, cards));
    }
    return result;
  }
  public void writeAllE3(List<ExpansionFile3> data, String outputDirectory) throws IOException {
    new File(outputDirectory).mkdirs();
    // write expansions file
//    String expansionsFileName = outputDirectory+File.separator+"expansions.yaml";
//    mapper.writeValue(new OutputStreamWriter(Files.newOutputStream(Paths.get(expansionsFileName)), StandardCharsets.UTF_8), new Expansions(data.expansions));
    // write individual expansion files. expansion-->cards
    for (ExpansionFile3 expansionFile : data) {
      for (Card3 card : expansionFile.getCards()) {
        if (card.getMoves() != null) {
          for (Move move : card.getMoves()) {
            move.setDamage(StringUtils.trimToNull(move.getDamage()));
            move.setText(StringUtils.trimToNull(move.getText()));
          }
        }
      }
      String filename = expansionFile.generateFinalFilePath(outputDirectory);
      Writer out = new OutputStreamWriter(Files.newOutputStream(Paths.get(filename)), StandardCharsets.UTF_8);
      mapper.writeValue(out, expansionFile);
//      String dump = yaml.dumpAs(expansion, Tag.MAP, null);
//      out.write(dump);
      out.close();
    }
  }

  public Collection<Expansion> prepareSetFiles(List<Card> cards) {
    Map<String, Expansion> expansionMap = new HashMap<>();
    for (Card card : cards) {
      String key = card.getExpansion().getEnumId();
      if (!expansionMap.containsKey(key)) {
        Expansion expansion = new Expansion();
        card.getExpansion().copyStaticPropertiesTo(expansion);
        expansion.setCards(new ArrayList<>());
        expansionMap.put(key, expansion);
      }
      expansionMap.get(key).getCards().add(card);
    }
    for (Expansion expansion : expansionMap.values()) {
      Comparator<Card> cardComparator = (o1, o2) -> {
        try {
          Integer n1 = Integer.parseInt(o1.getNumber());
          Integer n2 = Integer.parseInt(o2.getNumber());
          return n1.compareTo(n2);
        } catch (NumberFormatException e) {
          return o1.getNumber().compareTo(o2.getNumber());
        }
      };
      expansion.getCards().sort(cardComparator);
    }
    return expansionMap.values();
  }

  public void prepareReprints(Collection<Expansion> expansionFiles) {
    Map<EqualityCard, Card> map = new HashMap<>();
    for (Expansion expansionFile : expansionFiles) {
      for (Card c : expansionFile.getCards()) {
//                int hash = Objects.hash(c.name, c.types, c.superType, c.subTypes, c.evolvesFrom, c.hp, c.retreatCost, c.abilities, c.moves, c.weaknesses, c.resistances, c.text, c.energy);
        EqualityCard ec = new EqualityCard(c);
        if (map.containsKey(ec)) {
          Card oc = map.get(ec);
          if (c.getRarity() == Rarity.ULTRA_RARE) {
            // most likely full art
            c.setVariantType(VariantType.FULL_ART);
          } else if (c.getRarity() == Rarity.SECRET) {
            // most likely secret art
            c.setVariantType(VariantType.SECRET_ART);
          } else {
            c.setVariantType(VariantType.REPRINT);
          }
          c.setVariantOf(oc.getId());
        } else {
          map.put(ec, c);
        }
      }
    }
  }

  public void fixGymSeriesEvolvesFromIssue(Collection<Expansion> expansions) {
    List<String> owners = Arrays.asList("Blaine's", "Brock's", "Misty's", "Lt. Surge's", "Sabrina's", "Erika's", "Koga's", "Giovanni's");
    for (Expansion expansion : expansions) {
      if(expansion.getName().contains("Gym ")){
        for (Card card : expansion.getCards()) {
          if(card.getSubTypes().contains(CardType.EVOLUTION)){
            for (String owner : owners) {
              if(card.getName().startsWith(owner)){
                if(card.getEvolvesFrom() == null){
                  System.out.println("NoEvolvesFrom:"+ card.getName());
                }
                if(!card.getEvolvesFrom().startsWith(owner)){
                  System.out.println(card.getName());
                  card.setEvolvesFrom(owner + " " + card.getEvolvesFrom());
                  break;
                }
              }
            }
          }
        }
      }
    }
  }
}