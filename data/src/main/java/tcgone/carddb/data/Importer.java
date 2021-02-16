/*
Copyright 2019 axpendix@hotmail.com

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
package tcgone.carddb.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tcgone.carddb.model.Expansion;
import tcgone.carddb.model.*;
import tcgone.carddb.model.Variant;
import tcgone.carddb.model.VariantType;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author axpendix@hotmail.com
 */
public class Importer {
  protected static final Logger log = LoggerFactory.getLogger(Importer.class);
  public static final String ID_PATTERN = "^[\\w-]+$";
  public static final String ID_RANGE_PATTERN = "^([\\w-]+)\\.\\.([\\w-]+)$";

  protected ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
  protected PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());

  protected List<Card> allCards;
  protected Map<String, Card> idToCard;
  protected Map<String, Card> seoNameToCard;
  protected Map<String, Card> pioIdToCard;
  protected Map<String, Card> cardInfoStringToCard;
  protected Map<String, Collection<Card>> variantsMap;

  protected List<Format> allFormats;
  protected Map<String, Format> idToFormat;

  protected List<Expansion> allExpansions;
  protected Map<String, Expansion> idToExpansion;

  protected Map<String, java.util.Set<String>> chains;

  public Importer() {
  }

  public void init() throws Exception {

    processCards();
    processEvolutionChains();
    processFormats();

  }

  private void validate(Expansion expansion, List<ConstraintViolation> violations) {

    if (isBlank(expansion.id) || !expansion.id.matches("^[0-9]{3}$"))
      violations.add(new ConstraintViolation("cards/"+ expansion.filename, "expansion.id missing or not acceptable, expansion ids must consist of three digits"));
    if (isBlank(expansion.name))
      violations.add(new ConstraintViolation("cards/"+ expansion.filename, "expansion.name missing"));
    if (isBlank(expansion.enumId))
      violations.add(new ConstraintViolation("cards/"+ expansion.filename, "expansion.enumId missing"));
    if (isBlank(expansion.abbr))
      violations.add(new ConstraintViolation("cards/"+ expansion.filename, "expansion.abbr missing"));

    if (expansion.cards == null || expansion.cards.isEmpty()) {
      violations.add(new ConstraintViolation("cards/"+ expansion.filename, "cards missing"));
      return;
    }

    String cardIdRegex = "^" + expansion.id + "-[\\w]+$";
    Pattern cardIdPattern = Pattern.compile(cardIdRegex);

    for (Card card : expansion.cards) {
      if (isBlank(card.id)) {
        violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/card/?", "id missing"));
        continue;
      }
      if (!cardIdPattern.matcher(card.id).matches())
        violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id, "id does not match pattern="+cardIdRegex));
      if (isBlank(card.name))
        violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id, "name missing"));
      if (isBlank(card.number))
        violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id, "number missing"));
      if (isBlank(card.enumId))
        violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id, "enumId missing"));
      if (card.superType == null || !card.superType.isSuperType())
        violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id, "superType missing or illegal"));
      if (card.rarity == null)
        violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id, "rarity missing"));

      if (card.superType == CardType.POKEMON) {
        if (card.hp == null && !card.subTypes.contains(CardType.LEGEND))
          violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id, "hp missing"));
        if (card.retreatCost == null && !card.subTypes.contains(CardType.LEGEND))
          violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id, "retreatCost missing"));
        if (card.subTypes.contains(CardType.STAGE1) || card.subTypes.contains(CardType.STAGE2)) {
          if(StringUtils.isEmpty(card.evolvesFrom)) {
            violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id, "evolvesFrom missing"));
          }
        }
        if (card.abilities != null)
          for (Ability ability : card.abilities) {
            if (isBlank(ability.name) || isBlank(ability.type) || isBlank(ability.text))
              violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id+"/abilities", "name, type or text missing"));
          }
        if (card.moves != null)
          for (Move move : card.moves) {
            if (isBlank(move.name))
              violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id+"/moves", "name missing"));
            if (move.cost == null)
              violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id+"/moves", "cost missing"));
          }
        if (card.weaknesses != null)
          for (WeaknessResistance wr : card.weaknesses) {
            if (isBlank(wr.value) || wr.type == null)
              violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id+"/weaknesses", "value or type missing"));
          }
        if (card.resistances != null)
          for (WeaknessResistance wr : card.resistances) {
            if (isBlank(wr.value) || wr.type == null)
              violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id+"/resistances", "value or type missing"));
          }

        // stage handling
        CardType stage = null;
        for (CardType cardType : CardType.allStages()) {
          if (card.subTypes.contains(cardType)) {
            if (stage != null) {
              if ((stage == CardType.BASIC && cardType == CardType.BABY) || stage == CardType.BABY && cardType == CardType.BASIC) {
                stage = CardType.BABY; // this is fine
              } else {
                violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id+"/subTypes", String.format("cannot have both: %s, %s", stage, cardType)));
              }
            } else {
              stage = cardType;
            }
          }
        }
        if (stage == null) {
          violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/"+card.id+"/subTypes", String.format("must have one: %s", CardType.allStages())));
        }
        if (stage == CardType.BABY && !card.subTypes.contains(CardType.BASIC)) {
          card.subTypes.add(CardType.BASIC);
        }
        card.stage = stage;

      }
      // TODO
      // check sub types
      // check empty/null fields, number, ordering, etc, attacks, abilities
    }

  }


  protected void processCards() throws IOException, ImportException {
    // read expansion files
    Resource[] resources = resourceResolver.getResources("classpath:/cards/*.yaml");
    List<Expansion> expansions = new ArrayList<>();
    for (Resource resource : resources) {
      log.trace("Reading {}", resource.getFilename());
      Expansion expansion = mapper.readValue(resource.getInputStream(), Expansion.class);
      expansion.filename = resource.getFilename();
      if ("E2".equals(expansion.schema)) {
        expansions.add(expansion);
      } else {
        log.warn("SKIPPING {}: IT DOESN'T HAVE THE EXPECTED SCHEMA", expansion.filename);
      }
    }
    if(expansions.size() < 100) {
      log.warn("THERE ARE ONLY {} EXPANSIONS IMPORTED. THERE SHOULD HAVE BEEN AT LEAST 100", expansions.size());
    }
    if(expansions.isEmpty()) {
      throw new ImportException("NO CARDS WERE FOUND");
    }

    allCards = new ArrayList<>();
    idToCard = new THashMap<>();
    pioIdToCard = new THashMap<>();
    seoNameToCard = new THashMap<>();
    cardInfoStringToCard = new THashMap<>();
    variantsMap = new THashMap<>();

    allExpansions = new ArrayList<>();
    idToExpansion = new THashMap<>();

    List<ConstraintViolation> violations = new ArrayList<>();

    for (Expansion expansion : expansions) {
      for (Card card : expansion.cards) {
        if (isBlank(card.id)) {
          violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/card/?", "id missing"));
          continue;
        }
        if (isNotBlank(card.copyOf)) {
          violations.add(new ConstraintViolation("cards/"+ expansion.filename+"/card/"+card.id, "copyOf field must be blank!"));
          continue;
        }
        idToCard.put(card.id, card);
      }
    }
    assertNoViolation(violations);

    variant_outer:
    for (Card card : idToCard.values()) {
      if (card.variantOf == null) {
        card.variantOf = card.id;
      }
      if (!card.variantOf.equals(card.id)) {
        THashSet<String> cycleDetector = new THashSet<>();
        Card current = card;
        cycleDetector.add(current.id);
        while (true) {
          if (Boolean.TRUE.equals(current.variantIsDifferent) && card.copyOf == null) {
            card.copyOf = current.id;
          }
          if (current.id.equals(current.variantOf) || current.variantOf == null) {
            if (Boolean.TRUE.equals(current.variantIsDifferent)) {
              violations.add(new ConstraintViolation("card/"+current.id, "when variantIsDifferent is TRUE, variantOf must always point to a different variant! "));
              continue variant_outer;
            }
            card.variantOf = current.id;
            if (card.copyOf == null) {
              card.copyOf = current.id;
            }
            break;
          }
          current = idToCard.get(current.variantOf);
          if (current == null) {
            violations.add(new ConstraintViolation("card/"+card.id, "variantOf does not point to a valid card"));
            continue variant_outer;
          }
          if (cycleDetector.contains(current.id)) {
            violations.add(new ConstraintViolation("card/"+card.id, "variantIds must not have cycles! "+cycleDetector));
            continue variant_outer;
          }
          cycleDetector.add(current.id);
        }
      }
      if (card.variantType == null) {
        card.variantType = !card.variantOf.equals(card.id) ? VariantType.REPRINT : VariantType.REGULAR;
      }
      variantsMap.computeIfAbsent(card.variantOf, s -> new THashSet<>()).add(card);
    }

    for (Card card : idToCard.values()) {
      if (card.id.equals(card.copyOf)) {
        card.copyOf = null;
      }
      if (card.copyOf != null) {
        Card base = idToCard.get(card.copyOf);

        if (card.name != null && !Objects.equals(base.name, card.name)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different name in copied card! "+base.name+", "+card.name));
        } else {
          card.name = base.name;
        }
        if (card.retreatCost != null && !Objects.equals(base.retreatCost, card.retreatCost)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different retreatCost in copied card! "+base.retreatCost+", "+card.retreatCost));
        } else {
          card.retreatCost = base.retreatCost;
        }
        if (card.types != null && !Objects.equals(base.types, card.types)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different types in copied card! "+base.types+", "+card.types));
        } else {
          card.types = base.types;
        }
        if (card.subTypes != null && !Objects.equals(base.subTypes, card.subTypes)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different subTypes in copied card! "+base.subTypes+", "+card.subTypes));
        } else {
          card.subTypes = base.subTypes;
        }
        if (card.superType != null && !Objects.equals(base.superType, card.superType)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different superType in copied card! "+base.superType+", "+card.superType));
        } else {
          card.superType = base.superType;
        }
        if (card.weaknesses != null && !Objects.equals(base.weaknesses, card.weaknesses)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different weaknesses in copied card! "+base.weaknesses+", "+card.weaknesses));
        } else {
          card.weaknesses = base.weaknesses;
        }
        if (card.resistances != null && !Objects.equals(base.resistances, card.resistances)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different resistances in copied card! "+base.resistances+", "+card.resistances));
        } else {
          card.resistances = base.resistances;
        }
        if (card.moves != null && !Objects.equals(base.moves, card.moves)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different moves in copied card! "+base.moves+", "+card.moves));
        } else {
          card.moves = base.moves;
        }
        if (card.abilities != null && !Objects.equals(base.abilities, card.abilities)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different abilities in copied card! "+base.abilities+", "+card.abilities));
        } else {
          card.abilities = base.abilities;
        }
        if (card.hp != null && !Objects.equals(base.hp, card.hp)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different hp in copied card! "+base.hp+", "+card.hp));
        } else {
          card.hp = base.hp;
        }
        if (card.evolvesTo != null && !Objects.equals(base.evolvesTo, card.evolvesTo)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different evolvesTo in copied card! "+base.evolvesTo+", "+card.evolvesTo));
        } else {
          card.evolvesTo = base.evolvesTo;
        }
        if (card.evolvesFrom != null && !Objects.equals(base.evolvesFrom, card.evolvesFrom)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different evolvesFrom in copied card! "+base.evolvesFrom+", "+card.evolvesFrom));
        } else {
          card.evolvesFrom = base.evolvesFrom;
        }
        if (card.stage != null && !Objects.equals(base.stage, card.stage)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different stage in copied card! "+base.stage+", "+card.stage));
        } else {
          card.stage = base.stage;
        }
        if (card.text != null && !Objects.equals(base.text, card.text)) {
//          violations.add(new ConstraintViolation("card/"+card.id, "different text in copied card! "+base.text+", "+card.text));
          // text changes between variants are fine
        } else {
          card.text = base.text;
        }
        if (card.energy != null && !Objects.equals(base.energy, card.energy)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different energy in copied card! "+base.energy+", "+card.energy));
        } else {
          card.energy = base.energy;
        }
        if (card.nationalPokedexNumber != null && !Objects.equals(base.nationalPokedexNumber, card.nationalPokedexNumber)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different nationalPokedexNumber in copied card! "+base.nationalPokedexNumber+", "+card.nationalPokedexNumber));
        } else {
          card.nationalPokedexNumber = base.nationalPokedexNumber;
        }
        if (card.erratas != null && !Objects.equals(base.erratas, card.erratas)) {
          violations.add(new ConstraintViolation("card/"+card.id, "different erratas in copied card! "+base.erratas+", "+card.erratas));
        } else {
          card.erratas = base.erratas;
        }
      }
    }

    assertNoViolation(violations);

    for (Expansion expansion : expansions) {
      int order = 1;

      validate(expansion, violations);
      if (!violations.isEmpty())
        continue;

      log.info("Processed {}", expansion.name);

      expansion.order = 1000 - Integer.parseInt(expansion.id);
      if(expansion.seoName == null)
        expansion.seoName = expansion.name.toLowerCase(Locale.ENGLISH).replaceAll("\\W+", "-");

      allExpansions.add(expansion);
      idToExpansion.put(expansion.id, expansion);
      idToExpansion.put(expansion.enumId, expansion);
      idToExpansion.put(expansion.seoName, expansion);

      for (Card card : expansion.cards) {
        card.expansion = expansion;
        if(expansion.notImplemented != null && expansion.notImplemented && !card.subTypes.contains(CardType.NOT_IMPLEMENTED)) {
          card.subTypes.add(CardType.NOT_IMPLEMENTED);
        }
        card.fullName = String.format("%s (%s %s)", card.name, card.expansion.abbr.toUpperCase(Locale.ENGLISH), card.number);
        // upgraded to uniform scan url scheme @ 09.08.2020
        card.imageUrl = String.format("https://tcgone.net/scans/m/%s/%s.jpg", card.expansion.enumId.toLowerCase(Locale.ENGLISH), card.number);
        card.imageUrlHiRes = String.format("https://tcgone.net/scans/l/%s/%s.jpg", card.expansion.enumId.toLowerCase(Locale.ENGLISH), card.number);
//                card.seoName = card.name.toLowerCase(Locale.ENGLISH).replaceAll("\\W","-");
//                card.seoName = String.format("%s-%s--%s", card.name.toLowerCase(Locale.ENGLISH).replaceAll("\\W+","-"), setFile.expansion.seoName, card.id);
        card.seoName = String.format("%s-%s-%s", card.name.replace("é", "e").replaceAll("\\W+", "-"), expansion.abbr, card.number).toLowerCase(Locale.ENGLISH);
        card.order = order++;
        card.order += expansion.order * 1000;
//                card.superType = Character.toTitleCase(card.superType.charAt(0)) + card.superType.substring(1).toLowerCase(Locale.ENGLISH);
        StringBuilder ftxb = new StringBuilder();
        String dlm = " • ";
        ftxb.append(card.name).append(dlm).append(card.expansion.name).append(" ").append(card.number).append(dlm)
          .append(card.superType).append(dlm).append(card.subTypes).append(dlm).append(card.rarity);
        if (card.abilities != null) {
          for (Ability ability : card.abilities) {
            ftxb.append(dlm).append(ability.type).append(": ").append(ability.name).append(dlm).append(ability.text);
          }
        }
        if (card.moves != null) {
          for (Move move : card.moves) {
            ftxb.append(dlm).append(move.name).append(": ");
            if (move.damage != null) {
              ftxb.append(move.damage).append(" damage. ");
            }
            if (move.text != null) {
              ftxb.append(move.text);
            }
          }
        }
        if (card.text != null) {
          for (String s : card.text) {
            ftxb.append(dlm).append(s);
          }
        }
        card.fullText = ftxb.toString();


        pioIdToCard.put(card.pioId, card);
        seoNameToCard.put(card.seoName, card);
        cardInfoStringToCard.put(card.enumId + ":" + expansion.enumId, card);
        allCards.add(card);

      } // end expansion

    } // end expansions

    assertNoViolation(violations);

    for (Collection<Card> cards : variantsMap.values()) {
      List<Variant> variants = new ArrayList<>();
      for (Card card : cards) {
        Variant variant = new Variant();
        variant.id = card.id;
        variant.type = card.variantType;
        variant.copyId = card.copyOf != null ? card.copyOf : card.id;
        variants.add(variant);
      }
//      if (variants.size() > 1) {
//        System.out.println(variantId+","+name+","+variants);
//      }
      for (Card card : cards) {
        card.variants = variants; // make it immutable?
      }
    }

    log.info("Imported all cards");
  }

  private void assertNoViolation(List<ConstraintViolation> violations) throws ImportException {
    if (!violations.isEmpty()) {
      throw new ImportException("Validation failed: \n" + violations.stream().map(ConstraintViolation::toString).collect(Collectors.joining("\n")));
    }
  }


  protected void processFormats() throws IOException, ImportException {

    allFormats = new ArrayList<>();
    idToFormat = new THashMap<>();

    List<Format> formatsFromFile = mapper.readValue(resourceResolver.getResource("classpath:/formats.yaml").getInputStream(), new TypeReference<List<Format>>() {});
    Pattern idRangePattern = Pattern.compile(ID_RANGE_PATTERN);

    List<ConstraintViolation> violations = new ArrayList<>();

    for (Format format : formatsFromFile) {
      if (format.flags != null && format.flags.contains("disabled")) {
        continue;
      }

      if(isBlank(format.enumId)) {
        violations.add(new ConstraintViolation("format/?", "enumId is missing"));
        continue;
      }
      if(isBlank(format.seoName)) {
        violations.add(new ConstraintViolation("format/"+ format.enumId, "seoName is missing"));
        continue;
      }
      if(isBlank(format.name))
        violations.add(new ConstraintViolation("format/"+ format.enumId, "name is missing"));
      if(isBlank(format.description))
        violations.add(new ConstraintViolation("format/"+ format.enumId, "description is missing"));
      if(format.expansions == null || format.expansions.isEmpty())
        violations.add(new ConstraintViolation("format/"+ format.enumId, "expansions missing"));
      if(isBlank(format.ruleSet))
        violations.add(new ConstraintViolation("format/"+ format.enumId, "ruleSet is missing"));

      // expand all ranges
      Function<Stream<String>, Stream<String>> expandRanges =
        (stream) -> stream.flatMap(s -> {
          Matcher matcher = idRangePattern.matcher(s);
          if(matcher.find()){
            String startId = matcher.group(1);
            String endId = matcher.group(2);
            log.info("Range detected: {}, start={}, end={}", s, startId, endId);
            Card startCard = idToCard.get(startId);
            if(startCard == null) {
              throw new IllegalStateException("Cannot find card " + startId);
            }
            int startIndex = startCard.expansion.cards.indexOf(startCard);
            List<String> accumulator = new ArrayList<>();
            while (startIndex < startCard.expansion.cards.size()) {
              Card card = startCard.expansion.cards.get(startIndex);
              accumulator.add(card.id);
              if(card.id.equals(endId)) {
                break;
              }
              startIndex++;
            }
            log.info("Expanded range {} to {}", s, accumulator);
            return accumulator.stream();
          } else {
            // single id, leave it
            return Stream.of(s);
          }
        });
      format.includes = format.includes != null ? expandRanges.apply(format.includes.stream()).collect(Collectors.toList()) : new ArrayList<>();
      format.excludes = format.excludes != null ? expandRanges.apply(format.excludes.stream()).collect(Collectors.toList()) : new ArrayList<>();

      // find all cards of each format

      String id = format.seoName;
      LinkedHashSet<Expansion> expansions = new LinkedHashSet<>();
      LinkedHashSet<Expansion> inclusionExpansions = new LinkedHashSet<>();
      LinkedHashSet<Card> cards = new LinkedHashSet<>();


      for (String setId : format.expansions) {
        Expansion expansion = idToExpansion.get(setId);
        if(expansion == null) {
          violations.add(new ConstraintViolation("format/"+ format.enumId, "expansion cannot be found "+setId));
          continue;
        }
        expansions.add(expansion);
      }

      for (String include : format.includes) {
        Card card = idToCard.get(include);
        if(card == null) {
          violations.add(new ConstraintViolation("format/"+ format.enumId, "include cannot be found "+include));
          continue;
        }
        inclusionExpansions.add(card.expansion);
        cards.add(card);
      }

      for (Expansion expansion : expansions) {
        if (!inclusionExpansions.contains(expansion)) {
          cards.addAll(expansion.cards);
        }
      }

      for (String exclude : format.excludes) {
        Card card = idToCard.get(exclude);
        if(card == null) {
          violations.add(new ConstraintViolation("format/"+ format.enumId, "exclude cannot be found "+exclude));
          continue;
        }
        if (inclusionExpansions.contains(card.expansion)) {
          violations.add(new ConstraintViolation("format/"+ format.enumId, "includes and excludes cannot be specified for cards from the expansion "+card.expansion.name));
          continue;
        }
        if (!cards.remove(card)) {
          violations.add(new ConstraintViolation("format/"+ format.enumId, "exclude was not included at all "+exclude));
        }
      }

      // add format to each valid card

      for (Card card : cards) {
        if (card.formats == null)
          card.formats = new ArrayList<>();

        card.formats.add(id);
      }

      idToFormat.put(id, format);
      idToFormat.put(format.enumId, format);
      allFormats.add(format);
    }

    if (!violations.isEmpty()) {
      throw new ImportException(violations.stream().map(ConstraintViolation::toString).collect(Collectors.toList()));
    }

  }

  protected void processEvolutionChains() throws ImportException {
    chains = new THashMap<>();
    HashSet<String> pokemonNames = new HashSet<>();

    for (Card card : allCards) {
      if (StringUtils.isNotEmpty(card.evolvesFrom)) {
        addToChain(card.evolvesFrom, card.name);
      }
      if (card.superType == CardType.POKEMON || card.subTypes.contains(CardType.FOSSIL)) {
        pokemonNames.add(card.name);
      }
    }

    List<String> errors = new ArrayList<>();
    for (Card card : allCards) {
      if (StringUtils.isNotEmpty(card.evolvesFrom)) {
        if (!pokemonNames.contains(card.evolvesFrom)) {
          errors.add(String.format("Cannot find card %s referenced via evolvesFrom in %s %s", card.evolvesFrom, card.id, card.expansion.name));
        }
      }
    }

    for (Map.Entry<String, java.util.Set<String>> entry : chains.entrySet()) {
      if (entry.getValue().size() >= 2) {
        String msg = String.format("Multi mappings have been found. %s:%s", entry.getKey(), entry.getValue());
        log.warn(msg);
        if (!entry.getValue().toString().contains("Fossil")) {
          errors.add(msg);
        }
      }
    }
    if (!errors.isEmpty())
      throw new ImportException(String.join("\n", errors));
  }

  protected void addToChain(String... a) {
    if (a.length == 3) {
      chains.computeIfAbsent(a[2], k -> new HashSet<>()).add(a[1]);
      chains.computeIfAbsent(a[1], k -> new HashSet<>()).add(a[0]);
    } else if (a.length == 2) {
      chains.computeIfAbsent(a[1], k -> new HashSet<>()).add(a[0]);
    }
  }

  public List<Card> getAllCards() {
    return allCards;
  }
}
