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
package net.tcgone.carddb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.*;
import gnu.trove.map.hash.THashMap;
import net.tcgone.carddb.model.Set;
import net.tcgone.carddb.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author axpendix@hotmail.com
 */
public class Importer {
  protected static final Logger log = LoggerFactory.getLogger(Importer.class);

  protected ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
  protected PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
  protected Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  protected List<Card> allCards;
  protected Map<String, Card> idToCard;
  protected Map<String, Card> seoNameToCard;
  protected Map<String, Card> pioIdToCard;
  protected Map<String, Card> cardInfoStringToCard;

  protected List<Format> allFormats;
  protected Map<String, Format> idToFormat;

  protected List<Set> allSets;
  protected Map<String, Set> idToSet;

  protected Multimap<String, String> chains;

  public Importer() {
  }

  public void init() throws Exception {

    processCards();
    processEvolutionChains();
    processFormats();

  }

  private void validateAndAssert(String context, Object o) throws ImportException {
    List<String> errors = new ArrayList<>();
    java.util.Set<ConstraintViolation<Object>> violations = validator.validate(o);
    for (ConstraintViolation<Object> cv : violations) {
      errors.add(String.format("%s %s %s", context, cv.getPropertyPath().toString(), cv.getMessage()));
    }
    if(o instanceof SetFile) { // then validate each card individually
      SetFile setFile = (SetFile) o;
      for (Card card : setFile.cards) {
        try {
          validateCard(setFile.filename + "/" + card.id, card);
        } catch (ImportException e) {
          errors.add(e.getMessage());
        }
      }
    }
    if (!errors.isEmpty()) {
      throw new ImportException(errors);
    }
  }

  protected void validateCard(String context, Card card) throws ImportException {
    if (!card.superType.isSuperType()) {
      throw new ImportException(String.format("Illegal superType %s in %s", card.superType, context));
    }
    if (card.superType == CardType.POKEMON) {
      if (card.hp == null && !card.subTypes.contains(CardType.LEGEND))
        throw new ImportException(String.format("Missing HP in %s", context));
      if (card.retreatCost == null && !card.subTypes.contains(CardType.LEGEND))
        throw new ImportException(String.format("Missing retreatCost in %s", context));
      if (card.subTypes.contains(CardType.STAGE1) || card.subTypes.contains(CardType.STAGE2)) {
        if(StringUtils.isEmpty(card.evolvesFrom)) {
          throw new ImportException(String.format("Missing evolvesFrom in %s", context));
        }
      }
    }
    // TODO
    // check sub types
    // check empty/null fields, number, ordering, etc, attacks, abilities
  }


  protected void processCards() throws IOException, ImportException {
    // read set files
    Resource[] resources = resourceResolver.getResources("classpath:/cards/*.yaml");
    List<SetFile> setFiles = new ArrayList<>();
    for (Resource resource : resources) {
      log.trace("Reading {}", resource.getFilename());
      SetFile setFile = mapper.readValue(resource.getInputStream(), SetFile.class);
      setFile.filename = resource.getFilename();
      setFiles.add(setFile);
    }

    allCards = new ArrayList<>();
    idToCard = new THashMap<>();
    pioIdToCard = new THashMap<>();
    seoNameToCard = new THashMap<>();
    cardInfoStringToCard = new THashMap<>();

    allSets = new ArrayList<>();
    idToSet = new THashMap<>();

    for (SetFile setFile : setFiles) {
      int order = 1;

      validateAndAssert(setFile.filename, setFile); //validate
      log.info("Validated {}", setFile.set.name);

      Set set = setFile.set;
      set.order = 1000 - Integer.parseInt(set.id);
      if(set.seoName == null)
        set.seoName = set.name.toLowerCase(Locale.ENGLISH).replaceAll("\\W+", "-");
      set.cards = ImmutableList.copyOf(setFile.cards);

      allSets.add(set);
      idToSet.put(set.id, set);
      idToSet.put(set.enumId, set);
      idToSet.put(set.seoName, set);

      for (Card card : set.cards) {
        card.set = set;
        if(set.notImplemented && !card.subTypes.contains(CardType.NOT_IMPLEMENTED)) {
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

    log.info("Imported all cards");
  }


  protected void processFormats() throws IOException, ImportException {

    List<Format> formatsFromFile = mapper.readValue(resourceResolver.getResource("classpath:/formats.yaml").getInputStream(), new TypeReference<List<Format>>() {});
    Pattern idRangePattern = Pattern.compile(Card.ID_RANGE_PATTERN);

    for (Format format : formatsFromFile) { //validate
      validateAndAssert(format.seoName, format);
    }

    allFormats = new ArrayList<>();
    idToFormat = new THashMap<>();

    for (Format format : formatsFromFile) {
      if (format.flags != null && format.flags.contains("disabled")) {
        continue;
      }

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
      format.includes = expandRanges.apply(format.includes.stream()).collect(Collectors.toList());
      format.excludes = expandRanges.apply(format.excludes.stream()).collect(Collectors.toList());

      // find all cards of each format

      String id = format.seoName;
      LinkedHashSet<Set> sets = new LinkedHashSet<>();
      LinkedHashSet<Set> inclusionSets = new LinkedHashSet<>();
      LinkedHashSet<Card> cards = new LinkedHashSet<>();


      for (String setId : format.sets) {
        Set set = idToSet.get(setId);
        if(set == null) {
          throw new ImportException(String.format("set '%s' defined in format '%s' cannot be found", setId, id));
        }
        sets.add(set);
      }

      for (String include : format.includes) {
        Card card = idToCard.get(include);
        if(card == null) {
          throw new ImportException(String.format("include '%s' defined in format '%s' cannot be found", include, id));
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
          throw new ImportException(String.format("exclude '%s' defined in format '%s' cannot be found", exclude, id));
        }
        if (inclusionSets.contains(card.set)) {
          throw new ImportException(String.format("includes and excludes cannot be specified for cards from the set %s for format %s", card.set.name, id));
        }
        if (!cards.remove(card)) {
          throw new ImportException(String.format("exclude '%s' defined in format '%s' was not included at all", exclude, id));
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
  }

  protected void processEvolutionChains() throws ImportException {
    chains = HashMultimap.create();
    Multiset<String> pokemonNames = HashMultiset.create();

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

    for (Map.Entry<String, Collection<String>> entry : chains.asMap().entrySet()) {
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
      chains.put(a[2], a[1]);
      chains.put(a[1], a[0]);
    } else if (a.length == 2) {
      chains.put(a[1], a[0]);
    }
  }

  public List<Card> getAllCards() {
    return allCards;
  }
}
