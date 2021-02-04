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
import tcgone.carddb.model.Set;
import tcgone.carddb.model.*;
import tcgone.carddb.model.VariantType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.*;


/**
 * @author axpendix@hotmail.com
 */
public class SetWriter {
  private final YAMLMapper mapper;

  public static class EqualityCard extends Card {

    public EqualityCard(Card card) {
      this.name = card.name;
      this.types = card.types;
      this.superType = card.superType;
      this.subTypes = card.subTypes;
      this.evolvesFrom = card.evolvesFrom;
      this.hp = card.hp;
      this.retreatCost = card.retreatCost;
      this.abilities = card.abilities;
      this.moves = card.moves;
      this.weaknesses = card.weaknesses;
      this.resistances = card.resistances;
      this.text = card.text;
      this.energy = card.energy;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      EqualityCard card = (EqualityCard) o;
      return Objects.equals(name, card.name) &&
        Objects.equals(types, card.types) &&
        Objects.equals(superType, card.superType) &&
        Objects.equals(subTypes, card.subTypes) &&
        Objects.equals(evolvesFrom, card.evolvesFrom) &&
        Objects.equals(hp, card.hp) &&
        Objects.equals(retreatCost, card.retreatCost) &&
        Objects.equals(abilities, card.abilities) &&
        Objects.equals(moves, card.moves) &&
        Objects.equals(weaknesses, card.weaknesses) &&
        Objects.equals(resistances, card.resistances) &&
        Objects.equals(text, card.text) &&
        Objects.equals(energy, card.energy);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, types, superType, subTypes, evolvesFrom, hp, retreatCost, abilities, moves, weaknesses, resistances, text, energy);
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
  public void writeAll(Collection<Set> sets,String outputDirectory) throws IOException {
    new File(outputDirectory).mkdirs();
    for (Set set : sets) {
      set.filename = String.format(outputDirectory+File.separator+"%s-%s.yaml", set.id, set.enumId.toLowerCase(Locale.ENGLISH));
      for (Card card : set.cards) {
        card.set = null;
        card.merged = null;
        card.formats = null;
        if (card.moves != null) {
          for (Move move : card.moves) {
            if (move.damage != null && move.damage.isEmpty()) {
              move.damage = null;
            }
            if (move.text != null && move.text.isEmpty()) {
              move.text = null;
            }
            //          if (move.cost != null && move.cost.size() == 1 && move.cost.get(0) == null) {
            //            move.cost = new ArrayList<>();
            //          }
          }
        }
      }
      BufferedWriter out = new BufferedWriter
        (new OutputStreamWriter(new FileOutputStream(set.filename), StandardCharsets.UTF_8));
      set.filename=null;
      mapper.writeValue(out,set);
//      String dump = yaml.dumpAs(set, Tag.MAP, null);
//      out.write(dump);
      out.close();
    }
  }

  public Collection<Set> prepareSetFiles(List<Card> cards) {
    Map<String, Set> expansionMap = new HashMap<>();
    for (Card card : cards) {
      String key = card.set.enumId;
      if (!expansionMap.containsKey(key)) {
        Set set = new Set();
        card.set.copyStaticPropertiesTo(set);
        set.cards = new ArrayList<>();
        expansionMap.put(key, set);
      }
      expansionMap.get(key).cards.add(card);
    }
    for (Set set : expansionMap.values()) {
      set.cards.sort((o1, o2) -> {
        try {
          Integer n1 = Integer.parseInt(o1.number);
          Integer n2 = Integer.parseInt(o2.number);
          return n1.compareTo(n2);
        } catch (NumberFormatException e) {
          return o1.number.compareTo(o2.number);
        }
      });
    }
    return expansionMap.values();
  }

  public void prepareReprints(Collection<Set> setFiles) {
    Map<EqualityCard, Card> map = new HashMap<>();
    for (Set setFile : setFiles) {
      for (Card c : setFile.cards) {
//                int hash = Objects.hash(c.name, c.types, c.superType, c.subTypes, c.evolvesFrom, c.hp, c.retreatCost, c.abilities, c.moves, c.weaknesses, c.resistances, c.text, c.energy);
        EqualityCard ec = new EqualityCard(c);
        if (map.containsKey(ec)) {
          Card oc = map.get(ec);
          if (c.rarity == Rarity.ULTRA_RARE) {
            // most likely full art
            c.variantType = VariantType.FULL_ART;
          } else if (c.rarity == Rarity.SECRET) {
            // most likely secret art
            c.variantType = VariantType.SECRET_ART;
          } else {
            c.variantType = VariantType.REPRINT;
          }
          c.variantOf = oc.id;
        } else {
          map.put(ec, c);
        }
      }
    }
  }

  public void fixGymSeriesEvolvesFromIssue(Collection<Set> sets) {
    List<String> owners = Arrays.asList("Blaine's", "Brock's", "Misty's", "Lt. Surge's", "Sabrina's", "Erika's", "Koga's", "Giovanni's");
    for (Set set : sets) {
      if(set.name.contains("Gym ")){
        for (Card card : set.cards) {
          if(card.subTypes.contains(CardType.EVOLUTION)){
            for (String owner : owners) {
              if(card.name.startsWith(owner)){
                if(card.evolvesFrom == null){
                  System.out.println("NoEvolvesFrom:"+card.name);
                }
                if(!card.evolvesFrom.startsWith(owner)){
                  System.out.println(card.name);
                  card.evolvesFrom = owner + " " + card.evolvesFrom;
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