package tcgone.carddb.tools;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.util.NodeStyleResolver;
import com.fasterxml.jackson.dataformat.yaml.util.StringQuotingChecker;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.lang3.StringUtils;
import tcgone.carddb.model.*;
import tcgone.carddb.model.Card;
import tcgone.carddb.model.ExpansionFile;

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
  public void writeAllE3(List<ExpansionFile> data, String outputDirectory) throws IOException {
    new File(outputDirectory).mkdirs();
    // write individual expansion files. expansion-->cards
    for (ExpansionFile expansionFile : data) {
      for (Card card : expansionFile.getCards()) {
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

//  public List<Expansion> prepareAndOrderExpansionFiles(List<Card> cards) {
//    Map<String, Expansion> expansionMap = new HashMap<>();
//    for (Card card : cards) {
//      String key = card.getExpansion().getEnumId();
//      if (!expansionMap.containsKey(key)) {
//        Expansion expansion = new Expansion();
//        card.getExpansion().copyStaticPropertiesTo(expansion);
//        expansion.setCards(new ArrayList<>());
//        expansionMap.put(key, expansion);
//      }
//      expansionMap.get(key).getCards().add(card);
//    }
//    for (Expansion expansion : expansionMap.values()) {
//      Comparator<Card> cardComparator = (o1, o2) -> {
//        try {
//          Integer n1 = Integer.parseInt(o1.getNumber());
//          Integer n2 = Integer.parseInt(o2.getNumber());
//          return n1.compareTo(n2);
//        } catch (NumberFormatException e) {
//          return o1.getNumber().compareTo(o2.getNumber());
//        }
//      };
//      expansion.getCards().sort(cardComparator);
//    }
//    List<Expansion> orderedList = new ArrayList<>(expansionMap.values());
//    orderedList.sort(Comparator.comparing(Expansion::getId));
//    return orderedList;
//  }

  /**
   * @param allCards must be ordered by release date first
   */
  public void detectAndSetReprintsWithEnhancedCards(List<EnhancedCard> allCards) {
    // fullText --> Card
    Map<String, EnhancedCard> map = new HashMap<>();
    for (EnhancedCard card : allCards) {
      String fullText = card.generateDiscriminatorFullText();
      if (map.containsKey(fullText)) {
        EnhancedCard original = map.get(fullText);
        // full-art, secret-art, holo can only be assigned if the original card is in the same expansion
        if (original.getExpansion().equals(card.getExpansion())) {

          // for some expansions (mostly classic), original (holo) is listed before regular version
          if (original.getRarity() == Rarity.RARE_HOLO && card.getRarity() == Rarity.RARE) {
            original.setVariantType(VariantType.HOLO);
            card.setVariantType(VariantType.REGULAR);
          }
          else if (card.getRarity() == Rarity.PROMO) {
            card.setVariantType(VariantType.PROMO);
          } else if (card.getRarity() == Rarity.ULTRA_RARE) {
            card.setVariantType(VariantType.FULL_ART);
          } else if (card.getRarity() == Rarity.SECRET) {
            card.setVariantType(VariantType.SECRET_ART);
          } else if (card.getRarity() == Rarity.RARE_HOLO) {
            card.setVariantType(VariantType.HOLO);
          } else {
            card.setVariantType(VariantType.ALTERNATE_ART);
          }
        } else { // reprint and promo possible in different expansions
          if (card.getRarity() == Rarity.PROMO) {
            card.setVariantType(VariantType.PROMO);
          } else {
            card.setVariantType(VariantType.REPRINT);
          }
        }
        card.setVariantOf(original.getEnumId());
      } else {
        map.put(fullText, card);
      }
    }
  }

  public void replaceEnumId(Card card, String clause, String replacement) {
    if (card.getEnumId().contains(clause)) {
      card.setEnumId(card.getEnumId().replace(clause, replacement));
    }
  }

  public void applyMiscFixes(List<ExpansionFile> expansionFiles) {
    for (ExpansionFile expansionFile : expansionFiles) {
      Expansion expansion = expansionFile.getExpansion();
//      expansionFile.getExpansion().setIsFanMade(null);
      for (Card card : expansionFile.getCards()) {
        replaceEnumId(card, "(DELTA_SPECIES)", "DELTA");
        replaceEnumId(card, "Δ", "DELTA");
        replaceEnumId(card, "a:", "A:");
        replaceEnumId(card, "b:", "B:");
        replaceEnumId(card, "♀", "_FEMALE");
        replaceEnumId(card, "♂", "_MALE");
        replaceEnumId(card, "&", "AND");
        replaceEnumId(card, "+", "PLUS");
        replaceEnumId(card, "'", "_");
        replaceEnumId(card, "'", "_");
        replaceEnumId(card, "◇", "PRISM_STAR");
//        card.setVariantType(null);
//        card.setVariantOf(null);
      }
    }
  }

  /**
   * @param expansionFiles must be ordered by release date first
   */
  public void detectAndSetReprints(List<ExpansionFile> expansionFiles) {
    // fullText --> Card
    Map<String, Card> map = new HashMap<>();
    Map<Card, Expansion> cardToExpansion = new HashMap<>();
    for (ExpansionFile expansionFile : expansionFiles) {
      Expansion expansion = expansionFile.getExpansion();
      for (Card card : expansionFile.getCards()) {
        cardToExpansion.put(card, expansion);
        String fullText = card.generateDiscriminatorFullText();
        if (map.containsKey(fullText)) {
          Card original = map.get(fullText);
          // full-art, secret-art, holo can only be assigned if the original card is in the same expansion
          if (cardToExpansion.get(original).equals(expansion)) {
            // for some expansions (mostly classic), original (holo) is listed before regular version
            if (original.getRarity() == Rarity.RARE_HOLO && card.getRarity() == Rarity.RARE) {
              original.setVariantType(VariantType.HOLO);
              card.setVariantType(VariantType.REGULAR);
            }
            else if (card.getRarity() == Rarity.ULTRA_RARE) {
              card.setVariantType(VariantType.FULL_ART);
            } else if (card.getRarity() == Rarity.SECRET) {
              card.setVariantType(VariantType.SECRET_ART);
            } else if (card.getRarity() == Rarity.RARE_HOLO) {
              card.setVariantType(VariantType.HOLO);
            } else {
              card.setVariantType(VariantType.ALTERNATE_ART);
            }
          } else { // reprint and promo possible in different expansions
            if (card.getRarity() == Rarity.PROMO) {
              card.setVariantType(VariantType.PROMO);
            } else {
              card.setVariantType(VariantType.REPRINT);
            }
          }
          card.setVariantOf(original.getEnumId());
        } else {
          map.put(fullText, card);
        }
      }
    }
  }

  public void fixGymSeriesEvolvesFromIssue(List<ExpansionFile> expansionFiles) {
    List<String> owners = Arrays.asList("Blaine's", "Brock's", "Misty's", "Lt. Surge's", "Sabrina's", "Erika's", "Koga's", "Giovanni's");
    for (ExpansionFile ef : expansionFiles) {
      if(ef.getExpansion().getName().contains("Gym ")){
        for (Card card : ef.getCards()) {
          if(card.getCardTypes().contains(CardType.EVOLUTION)){
            for (String owner : owners) {
              if(card.getName().startsWith(owner)){
                if(card.getEvolvesFrom() == null || card.getEvolvesFrom().isEmpty()){
                  System.out.println("NoEvolvesFrom:"+ card.getName());
                }
                if(!card.getEvolvesFrom().get(0).startsWith(owner)){
                  System.out.println(card.getName());
                  card.setEvolvesFrom(Collections.singletonList(owner + " " + card.getEvolvesFrom()));
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