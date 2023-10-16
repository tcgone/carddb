package tcgone.carddb.tools;

import com.expediagroup.beans.BeanUtils;
import com.expediagroup.transformer.model.FieldMapping;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.util.NodeStyleResolver;
import com.fasterxml.jackson.dataformat.yaml.util.StringQuotingChecker;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.lang3.StringUtils;
import tcgone.carddb.model.*;
import tcgone.carddb.model3.Card3;
import tcgone.carddb.model3.Expansion3;
import tcgone.carddb.model3.ExpansionFile3;

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
    Set<String> propertyNamesWithFlowStyle = new THashSet<>(Arrays.asList("cost", "types", "subTypes", "cardTypes", "evolvesTo", "evolvesFrom", "energy"));

    mapper = YAMLMapper.builder(
      YAMLFactory.builder()
        .nodeStyleResolver(s -> (propertyNamesWithFlowStyle.contains(s)) ? NodeStyleResolver.NodeStyle.FLOW : null)
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
      return new ObjectNode(this, new TreeMap<>());
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
  public List<ExpansionFile3> convertFromE2ToE3(Collection<Expansion> expansions) {
    List<ExpansionFile3> result = new ArrayList<>();
    BeanUtils beanUtils = new BeanUtils();
    for (Expansion expansion : expansions) {
      Expansion3 expansion3 = beanUtils.getTransformer()
        .withFieldMapping(new FieldMapping<>("id", "orderId"))
        .withFieldMapping(new FieldMapping<>("abbr", "shortName"))
        .setDefaultValueForMissingPrimitiveField(false)
        .skipTransformationForField("isFanMade")
        .transform(expansion, Expansion3.class);
      List<Card3> cards = new ArrayList<>();
      for (Card card : expansion.getCards()) {
        Card3 card3 = beanUtils.getTransformer()
          .skipTransformationForField("evolvesFrom", "text", "abilities", "moves", "expansionEnumId", "cardTypes", "energy")
          .setDefaultValueForMissingPrimitiveField(false)
          .transform(card, Card3.class);
        if (card.getEvolvesFrom() != null){
          card3.setEvolvesFrom(Collections.singletonList(card.getEvolvesFrom()));
        }
        if (card.getText() != null && !card.getText().isEmpty() ) {
          card3.setText(String.join("\n", card.getText()));
        }
        card3.setEnergy(card.getEnergy());
//        card3.setEnumId(card.getEnumId());
        card3.setEnumId(card.getEnumId()+":"+expansion3.getEnumId());
//        card3.setExpansionEnumId(expansion3.getEnumId());
        card3.setCardTypes(new ArrayList<>());
        card3.getCardTypes().add(card.getSuperType());
        if (card.getSubTypes() != null)
          card3.getCardTypes().addAll(card.getSubTypes());
        card3.setMoves(card.getMoves());
        card3.setAbilities(card.getAbilities());
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

  public List<Expansion> prepareAndOrderExpansionFiles(List<Card> cards) {
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
    List<Expansion> orderedList = new ArrayList<>(expansionMap.values());
    orderedList.sort(Comparator.comparing(Expansion::getId));
    return orderedList;
  }

  /**
   * @param expansionFiles must be ordered by release date
   */
  public void prepareReprints(List<Expansion> expansionFiles) {
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
  /**
   * @param expansionFiles must be ordered by release date
   */
  public void prepareReprintsE3(List<ExpansionFile3> expansionFiles) {
    Map<String, Card3> map = new HashMap<>();
    for (ExpansionFile3 expansionFile : expansionFiles) {
      for (Card3 card : expansionFile.getCards()) {
        String fullText = card.generateDiscriminatorFullText();
        if (map.containsKey(fullText)) {
          Card3 original = map.get(fullText);
          if (card.getRarity() == Rarity.ULTRA_RARE) {
            // most likely full art
            card.setVariantType(VariantType.FULL_ART);
          } else if (card.getRarity() == Rarity.SECRET) {
            // most likely secret art
            card.setVariantType(VariantType.SECRET_ART);
          } else {
            card.setVariantType(VariantType.REPRINT);
          }
          card.setVariantOf(original.getEnumId());
        } else {
          map.put(fullText, card);
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