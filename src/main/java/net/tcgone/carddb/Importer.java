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
import java.util.stream.Collectors;

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
  protected Map<String, Format> enumIdToFormat;
  protected Map<String, Format> seoNameToFormat;

  protected List<Set> allSets;
  protected Map<String, Set> enumIdToSet;
  protected Map<String, Set> seoNameToSet;

  protected Multimap<String, String> chains;

  public Importer() {
  }

  public void init() throws Exception {

    processFormats();
    processCards();
    processEvolutionChains();

  }

  private void validateAndAssert(String context, Object o) throws ImportException {
    java.util.Set<ConstraintViolation<Object>> violations = validator.validate(o);
    String message = violations.stream()
      .map(cv -> String.format("%s %s %s", context, cv.getPropertyPath().toString(), cv.getMessage()))
      .collect(Collectors.joining(","));
    if(!violations.isEmpty()) {
      throw new ImportException(message);
    }
  }

  protected void validateCard(String context, Card card) throws ImportException {
    if(!card.superType.isSuperType()) {
      throw new ImportException(String.format("Illegal superType %s in %s", card.superType, context));
    }
    if(card.superType==CardType.POKEMON) {
      if(card.hp==null && !card.subTypes.contains(CardType.LEGEND)) throw new ImportException(String.format("Missing HP in %s", context));
      if(card.retreatCost==null && !card.subTypes.contains(CardType.LEGEND)) throw new ImportException(String.format("Missing retreatCost in %s", context));
    }
    // TODO
    // check sub types
    // check empty/null fields, number, ordering, etc, attacks, abilities
  }


  protected void processFormats() throws IOException, ImportException {
    enumIdToFormat = new THashMap<>();
    seoNameToFormat = new THashMap<>();
    allFormats = mapper.readValue(resourceResolver.getResource("classpath:/formats.yaml").getInputStream(), new TypeReference<List<Format>>() {
    });
    for (Format format : allFormats) { //validate
      validateAndAssert(format.enumId, format);
    }
    allFormats = allFormats.stream(
    ).filter(f -> f.flags == null || !f.flags.contains("disabled")
    ).peek(f -> {
        f._sets = new ArrayList<>();
        f._cards = new ArrayList<>();
        seoNameToFormat.put(f.seoName, f);
        enumIdToFormat.put(f.enumId, f);
      }
    ).collect(Collectors.toList());
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
    enumIdToSet = new THashMap<>();
    seoNameToSet = new THashMap<>();

    for (SetFile setFile : setFiles) {
      int order = 1;

      validateAndAssert(setFile.filename, setFile); //validate
      log.info("Validated {}", setFile.set.name);

      Set set = setFile.set;
      set.order = 1000 - Integer.parseInt(set.id);
      set.seoName = set.name.toLowerCase(Locale.ENGLISH).replaceAll("\\W+", "-");
      set._formats = new ArrayList<>();
      set._cards = ImmutableList.copyOf(setFile.cards);

      allSets.add(set);
      seoNameToSet.put(set.seoName, set);
      enumIdToSet.put(set.enumId, set);

      for (Format format : allFormats) {
        /*
        relation of sets vs formats:
        if a set is specified inside format.sets, then it is displayed as part of that format.
        note that this is just a display. the actual card list of a format can be different than this.
        example: promo sets may not be mentioned inside sets clause, but included in the "includes" field.
        includes: if one card from a set is specified in includes field, it is assumed everything else in the same set is excluded.
        excludes: if one card from a set is specified in excludes field, it is assumed everything else in the same set is included.
        this means it is a violation to both specify includes and excludes for the same set.
         */
        boolean inclSet = false;
        boolean exclSet = false;
        boolean direSet = false;
        if (format.sets.contains(set.id)) {
          set._formats.add(format);
          format._sets.add(set);
          direSet = true;
        }
        for (Card card : set._cards) {
          if (format.includes.contains(card.id)) {
            inclSet = true;
          }
          if (format.excludes.contains(card.id)) {
            exclSet = true;
          }
        }
        if (inclSet && exclSet) {
          throw new ImportException(String.format("includes and excludes cannot be specified for cards from the set %s for format %s", set.name, format.name));
        }
        for (Card card : set._cards) {
          if (card.formats == null)
            card.formats = new ArrayList<>();
          if (inclSet && format.includes.contains(card.id)) {
            card.formats.add(format.seoName);
            format._cards.add(card);
          } else if (!inclSet && direSet && !format.excludes.contains(card.id)) {
            card.formats.add(format.seoName);
            format._cards.add(card);
          }
        }
      }

      for (Card card : set._cards) {
        validateCard(setFile.filename+"/"+card.id, card);
        card.set = set;
        card.fullName = String.format("%s (%s %s)", card.name, card.set.abbr.toUpperCase(Locale.ENGLISH), card.number);
        String paddedNumber;
        try {
//          Matcher numberExtractionMatcher = numberExtractionPattern.matcher(card.number);
//          if(numberExtractionMatcher.find()){
//            paddedNumber = String.format("%03d",Integer.parseInt(numberExtractionMatcher.group(1)));
//          } else {
//            paddedNumber = card.number;
//          }
          paddedNumber = String.format("%03d", Integer.parseInt(card.number));
        } catch (NumberFormatException e) {
          // this should not happen with the above matcher, if enabled
          // and yes we have to reorganize scans for all non-numbered cards
          paddedNumber = card.number;
        }
        card.imageUrl = String.format("https://tcgone.net/scans/m/%s/%s.jpg", card.set.enumId.toLowerCase(Locale.ENGLISH), paddedNumber);
        card.imageUrlHiRes = String.format("https://tcgone.net/scans/l/%s/%s.jpg", card.set.enumId.toLowerCase(Locale.ENGLISH), paddedNumber);
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


  protected void processEvolutionChains() throws ImportException {
    chains = HashMultimap.create();
    Multiset<String> pokemonNames = HashMultiset.create();

    for (Card card : allCards) {
      if(StringUtils.isNotEmpty(card.evolvesFrom)) {
        addToChain(card.evolvesFrom, card.name);
      }
      if(card.superType== CardType.POKEMON||card.subTypes.contains(CardType.FOSSIL)){
        pokemonNames.add(card.name);
      }
    }

    List<String> errors = new ArrayList<>();
    for (Card card : allCards) {
      if(StringUtils.isNotEmpty(card.evolvesFrom)) {
        if(!pokemonNames.contains(card.evolvesFrom)) {
          errors.add(String.format("Cannot find card %s referenced via evolvesFrom in %s %s", card.evolvesFrom, card.id, card.set.name));
        }
      }
    }

    for (Map.Entry<String, Collection<String>> entry : chains.asMap().entrySet()) {
      if(entry.getValue().size()>=2){
        String msg = String.format("Multi mappings have been found. %s:%s", entry.getKey(), entry.getValue());
        log.warn(msg);
        if(!entry.getValue().toString().contains("Fossil")){
          errors.add(msg);
        }
      }
    }
    if(!errors.isEmpty())
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


}
