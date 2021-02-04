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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tcgone.carddb.model.Set;
import tcgone.carddb.model.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author axpendix@hotmail.com
 */
public class Importer {
  protected static final Logger log = LoggerFactory.getLogger(Importer.class);

  protected ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
  protected PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());

  protected List<Card> allCards;
  protected Map<String, Card> idToCard;
  protected Map<String, Card> seoNameToCard;
  protected Map<String, Card> pioIdToCard;
  protected Map<String, Card> cardInfoStringToCard;

  protected List<Format> allFormats;
  protected Map<String, Format> idToFormat;

  protected List<Set> allSets;
  protected Map<String, Set> idToSet;

  protected Map<String, java.util.Set<String>> chains;

  public Importer() {
  }

  public void init() throws Exception {

    processCards();
    processEvolutionChains();
    processFormats();

  }

  private void validate(Set set) throws ImportException {
    List<ConstraintViolation> violations = new ArrayList<>();

    try {

      if (isBlank(set.id) || !set.id.matches("^[0-9]{3}$"))
        violations.add(new ConstraintViolation("cards/"+set.filename, "set.id missing or not acceptable, set ids must consist of three digits"));
      if (isBlank(set.name))
        violations.add(new ConstraintViolation("cards/"+set.filename, "set.name missing"));
      if (isBlank(set.enumId))
        violations.add(new ConstraintViolation("cards/"+set.filename, "set.enumId missing"));
      if (isBlank(set.abbr))
        violations.add(new ConstraintViolation("cards/"+set.filename, "set.abbr missing"));

      if (set.cards == null || set.cards.isEmpty()) {
        violations.add(new ConstraintViolation("cards/"+set.filename, "cards missing"));
        return;
      }

      String cardIdRegex = "^" + set.id + "-[\\w]+$";
      Pattern cardIdPattern = Pattern.compile(cardIdRegex);

      for (Card card : set.cards) {
        if (isBlank(card.id)) {
          violations.add(new ConstraintViolation("cards/"+set.filename+"/card/?", "id missing"));
          continue;
        }

        if (!cardIdPattern.matcher(card.id).matches())
          violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id, "id does not match pattern="+cardIdRegex));
        if (isBlank(card.name))
          violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id, "name missing"));
        if (isBlank(card.number))
          violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id, "number missing"));
        if (isBlank(card.enumId))
          violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id, "enumId missing"));
        if (card.superType == null || !card.superType.isSuperType())
          violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id, "superType missing or illegal"));
        if (card.rarity == null)
          violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id, "rarity missing"));

        if (card.superType == CardType.POKEMON) {
          if (card.hp == null && !card.subTypes.contains(CardType.LEGEND))
            violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id, "hp missing"));
          if (card.retreatCost == null && !card.subTypes.contains(CardType.LEGEND))
            violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id, "retreatCost missing"));
          if (card.subTypes.contains(CardType.STAGE1) || card.subTypes.contains(CardType.STAGE2)) {
            if(StringUtils.isEmpty(card.evolvesFrom)) {
              violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id, "evolvesFrom missing"));
            }
          }
          if (card.abilities != null)
            for (Ability ability : card.abilities) {
              if (isBlank(ability.name) || isBlank(ability.type) || isBlank(ability.text))
                violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id+"/abilities", "name, type or text missing"));
            }
          if (card.moves != null)
            for (Move move : card.moves) {
              if (isBlank(move.name))
                violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id+"/moves", "name missing"));
              if (move.cost == null)
                violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id+"/moves", "cost missing"));
            }
          if (card.weaknesses != null)
            for (WeaknessResistance wr : card.weaknesses) {
              if (isBlank(wr.value) || wr.type == null)
                violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id+"/weaknesses", "value or type missing"));
            }
          if (card.resistances != null)
            for (WeaknessResistance wr : card.resistances) {
              if (isBlank(wr.value) || wr.type == null)
                violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id+"/resistances", "value or type missing"));
            }

          // stage handling
          CardType stage = null;
          for (CardType cardType : CardType.allStages()) {
            if (card.subTypes.contains(cardType)) {
              if (stage != null) {
                if ((stage == CardType.BASIC && cardType == CardType.BABY) || stage == CardType.BABY && cardType == CardType.BASIC) {
                  stage = CardType.BABY; // this is fine
                } else {
                  violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id+"/subTypes", String.format("cannot have both: %s, %s", stage, cardType)));
                }
              } else {
                stage = cardType;
              }
            }
          }
          if (stage == null) {
            violations.add(new ConstraintViolation("cards/"+set.filename+"/"+card.id+"/subTypes", String.format("must have one: %s", CardType.allStages())));
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

    } finally {
      if (!violations.isEmpty()) {
        throw new ImportException(violations.stream().map(ConstraintViolation::toString).collect(Collectors.toList()));
      }
    }


  }


  protected void processCards() throws IOException, ImportException {
    // read set files
    Resource[] resources = resourceResolver.getResources("classpath:/cards/*.yaml");
    List<Set> sets = new ArrayList<>();
    for (Resource resource : resources) {
      log.trace("Reading {}", resource.getFilename());
      Set set = mapper.readValue(resource.getInputStream(), Set.class);
      set.filename = resource.getFilename();
      if ("E2".equals(set.schema)) {
        sets.add(set);
      } else {
        log.warn("SKIPPING {}: IT DOESN'T HAVE THE EXPECTED SCHEMA", set.filename);
      }
    }
    if(sets.size() < 100) {
      log.warn("THERE ARE ONLY {} SETS IMPORTED. THERE SHOULD HAVE BEEN AT LEAST 100", sets.size());
    }
    if(sets.isEmpty()) {
      throw new ImportException("NO CARDS WERE FOUND");
    }

    allCards = new ArrayList<>();
    idToCard = new THashMap<>();
    pioIdToCard = new THashMap<>();
    seoNameToCard = new THashMap<>();
    cardInfoStringToCard = new THashMap<>();

    allSets = new ArrayList<>();
    idToSet = new THashMap<>();

    boolean validationFailed = false;
    StringBuilder validationMessages = new StringBuilder();

    for (Set set : sets) {
      int order = 1;

      try {
        validate(set);
      } catch (ImportException e) {
        validationFailed = true;
        validationMessages.append(e.getMessage());
        continue;
      }
      log.info("Validated {}", set.name);

      set.order = 1000 - Integer.parseInt(set.id);
      if(set.seoName == null)
        set.seoName = set.name.toLowerCase(Locale.ENGLISH).replaceAll("\\W+", "-");
      set.cards = new ArrayList<>(set.cards);

      allSets.add(set);
      idToSet.put(set.id, set);
      idToSet.put(set.enumId, set);
      idToSet.put(set.seoName, set);

      for (Card card : set.cards) {
        card.set = set;
        if(set.notImplemented != null && set.notImplemented && !card.subTypes.contains(CardType.NOT_IMPLEMENTED)) {
          card.subTypes.add(CardType.NOT_IMPLEMENTED);
        }
        card.fullName = String.format("%s (%s %s)", card.name, card.set.abbr.toUpperCase(Locale.ENGLISH), card.number);
        // upgraded to uniform scan url scheme @ 09.08.2020
        card.imageUrl = String.format("https://tcgone.net/scans/m/%s/%s.jpg", card.set.enumId.toLowerCase(Locale.ENGLISH), card.number);
        card.imageUrlHiRes = String.format("https://tcgone.net/scans/l/%s/%s.jpg", card.set.enumId.toLowerCase(Locale.ENGLISH), card.number);
//                card.seoName = card.name.toLowerCase(Locale.ENGLISH).replaceAll("\\W","-");
//                card.seoName = String.format("%s-%s--%s", card.name.toLowerCase(Locale.ENGLISH).replaceAll("\\W+","-"), setFile.set.seoName, card.id);
        card.seoName = String.format("%s-%s-%s", card.name.replace("é", "e").replaceAll("\\W+", "-"), set.abbr, card.number).toLowerCase(Locale.ENGLISH);
        card.order = order++;
        card.order += set.order * 1000;
//                card.superType = Character.toTitleCase(card.superType.charAt(0)) + card.superType.substring(1).toLowerCase(Locale.ENGLISH);
        StringBuilder ftxb = new StringBuilder();
        String dlm = " • ";
        ftxb.append(card.name).append(dlm).append(card.set.name).append(" ").append(card.number).append(dlm)
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


        idToCard.put(card.id, card);
        pioIdToCard.put(card.pioId, card);
        seoNameToCard.put(card.seoName, card);
        cardInfoStringToCard.put(card.enumId + ":" + set.enumId, card);
        allCards.add(card);

      }

    }

    if (validationFailed) {
      throw new ImportException("Validation failed: " + validationMessages.toString());
    }
    log.info("Imported all cards");
  }


  protected void processFormats() throws IOException, ImportException {

    allFormats = new ArrayList<>();
    idToFormat = new THashMap<>();

    List<Format> formatsFromFile = mapper.readValue(resourceResolver.getResource("classpath:/formats.yaml").getInputStream(), new TypeReference<List<Format>>() {});
    Pattern idRangePattern = Pattern.compile(Card.ID_RANGE_PATTERN);

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
      if(format.sets == null || format.sets.isEmpty())
        violations.add(new ConstraintViolation("format/"+ format.enumId, "sets missing"));
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
            int startIndex = startCard.set.cards.indexOf(startCard);
            List<String> accumulator = new ArrayList<>();
            while (startIndex < startCard.set.cards.size()) {
              Card card = startCard.set.cards.get(startIndex);
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
      LinkedHashSet<Set> sets = new LinkedHashSet<>();
      LinkedHashSet<Set> inclusionSets = new LinkedHashSet<>();
      LinkedHashSet<Card> cards = new LinkedHashSet<>();


      for (String setId : format.sets) {
        Set set = idToSet.get(setId);
        if(set == null) {
          violations.add(new ConstraintViolation("format/"+ format.enumId, "set cannot be found "+setId));
          continue;
        }
        sets.add(set);
      }

      for (String include : format.includes) {
        Card card = idToCard.get(include);
        if(card == null) {
          violations.add(new ConstraintViolation("format/"+ format.enumId, "include cannot be found "+include));
          continue;
        }
        inclusionSets.add(card.set);
        cards.add(card);
      }

      for (Set set : sets) {
        if (!inclusionSets.contains(set)) {
          cards.addAll(set.cards);
        }
      }

      for (String exclude : format.excludes) {
        Card card = idToCard.get(exclude);
        if(card == null) {
          violations.add(new ConstraintViolation("format/"+ format.enumId, "exclude cannot be found "+exclude));
          continue;
        }
        if (inclusionSets.contains(card.set)) {
          violations.add(new ConstraintViolation("format/"+ format.enumId, "includes and excludes cannot be specified for cards from the set "+card.set.name));
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
          errors.add(String.format("Cannot find card %s referenced via evolvesFrom in %s %s", card.evolvesFrom, card.id, card.set.name));
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
