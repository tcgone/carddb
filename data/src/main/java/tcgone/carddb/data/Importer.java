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
  protected Map<String, Card> cardEnumIdToCard;
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

    if (isBlank(expansion.getId()) || !expansion.getId().matches("^[0-9]{3}$"))
      violations.add(new ConstraintViolation("cards/"+ expansion.getFilename(), "expansion.id missing or not acceptable, expansion ids must consist of three digits"));
    if (isBlank(expansion.getName()))
      violations.add(new ConstraintViolation("cards/"+ expansion.getFilename(), "expansion.name missing"));
    if (isBlank(expansion.getEnumId()))
      violations.add(new ConstraintViolation("cards/"+ expansion.getFilename(), "expansion.enumId missing"));
    if (isBlank(expansion.getAbbr()))
      violations.add(new ConstraintViolation("cards/"+ expansion.getFilename(), "expansion.abbr missing"));

    if (expansion.getCards() == null || expansion.getCards().isEmpty()) {
      violations.add(new ConstraintViolation("cards/"+ expansion.getFilename(), "cards missing"));
      return;
    }

    String cardIdRegex = "^" + expansion.getId() + "-[\\w]+$";
    Pattern cardIdPattern = Pattern.compile(cardIdRegex);

    for (Card card : expansion.getCards()) {
      if (isBlank(card.getId())) {
        violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/card/?", "id missing"));
        continue;
      }
      if (!cardIdPattern.matcher(card.getId()).matches())
        violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId(), "id does not match pattern="+cardIdRegex));
      if (isBlank(card.getName()))
        violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId(), "name missing"));
      if (isBlank(card.getNumber()))
        violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId(), "number missing"));
      if (isBlank(card.getEnumId()))
        violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId(), "enumId missing"));
      if (card.getSuperType() == null || !card.getSuperType().isSuperType())
        violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId(), "superType missing or illegal"));
      if (card.getRarity() == null)
        violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId(), "rarity missing"));

      if (card.getSuperType() == CardType.POKEMON) {
        if (card.getHp() == null && !card.getSubTypes().contains(CardType.LEGEND))
          violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId(), "hp missing"));
        if (card.getRetreatCost() == null && !card.getSubTypes().contains(CardType.LEGEND))
          violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId(), "retreatCost missing"));
        if (card.getSubTypes().contains(CardType.STAGE1) || card.getSubTypes().contains(CardType.STAGE2)) {
          if(StringUtils.isEmpty(card.getEvolvesFrom())) {
            violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId(), "evolvesFrom missing"));
          }
        }
        if (card.getAbilities() != null)
          for (Ability ability : card.getAbilities()) {
            if (isBlank(ability.getName()) || isBlank(ability.getType()) || isBlank(ability.getText()))
              violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId() +"/abilities", "name, type or text missing"));
          }
        if (card.getMoves() != null)
          for (Move move : card.getMoves()) {
            if (isBlank(move.getName()))
              violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId() +"/moves", "name missing"));
            if (move.getCost() == null)
              violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId() +"/moves", "cost missing"));
          }
        if (card.getWeaknesses() != null)
          for (WeaknessResistance wr : card.getWeaknesses()) {
            if (isBlank(wr.getValue()) || wr.getType() == null)
              violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId() +"/weaknesses", "value or type missing"));
          }
        if (card.getResistances() != null)
          for (WeaknessResistance wr : card.getResistances()) {
            if (isBlank(wr.getValue()) || wr.getType() == null)
              violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId() +"/resistances", "value or type missing"));
          }

        // stage handling
        CardType stage = null;
        for (CardType cardType : CardType.allStages()) {
          if (card.getSubTypes().contains(cardType)) {
            if (stage != null) {
              if ((stage == CardType.BASIC && cardType == CardType.BABY) || stage == CardType.BABY && cardType == CardType.BASIC) {
                stage = CardType.BABY; // this is fine
              } else {
                violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId() +"/subTypes", String.format("cannot have both: %s, %s", stage, cardType)));
              }
            } else {
              stage = cardType;
            }
          }
        }
        if (stage == null) {
          violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/"+ card.getId() +"/subTypes", String.format("must have one: %s", CardType.allStages())));
        }
        if (stage == CardType.BABY && !card.getSubTypes().contains(CardType.BASIC)) {
          card.getSubTypes().add(CardType.BASIC);
        }
        card.setStage(stage);

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
      expansion.setFilename(resource.getFilename());
      if ("E2".equals(expansion.getSchema())) {
        expansions.add(expansion);
      } else {
        log.warn("SKIPPING {}: IT DOESN'T HAVE THE EXPECTED SCHEMA", expansion.getFilename());
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
    cardEnumIdToCard = new THashMap<>();
    variantsMap = new THashMap<>();

    allExpansions = new ArrayList<>();
    idToExpansion = new THashMap<>();

    List<ConstraintViolation> violations = new ArrayList<>();

    for (Expansion expansion : expansions) {
      for (Card card : expansion.getCards()) {
        if (isBlank(card.getId())) {
          violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/card/?", "id missing"));
          continue;
        }
        if (isNotBlank(card.getCopyOf())) {
          violations.add(new ConstraintViolation("cards/"+ expansion.getFilename() +"/card/"+ card.getId(), "copyOf field must be blank!"));
          continue;
        }
        idToCard.put(card.getId(), card);
      }
    }
    assertNoViolation(violations);

    variant_outer:
    for (Card card : idToCard.values()) {
      if (card.getVariantOf() == null) {
        card.setVariantOf(card.getId());
      }
      if (!card.getVariantOf().equals(card.getId())) {
        THashSet<String> cycleDetector = new THashSet<>();
        Card current = card;
        cycleDetector.add(current.getId());
        while (true) {
          if (Boolean.TRUE.equals(current.getVariantIsDifferent()) && card.getCopyOf() == null) {
            card.setCopyOf(current.getId());
          }
          if (current.getId().equals(current.getVariantOf()) || current.getVariantOf() == null) {
            if (Boolean.TRUE.equals(current.getVariantIsDifferent())) {
              violations.add(new ConstraintViolation("card/"+ current.getId(), "when variantIsDifferent is TRUE, variantOf must always point to a different variant! "));
              continue variant_outer;
            }
            card.setVariantOf(current.getId());
            if (card.getCopyOf() == null) {
              card.setCopyOf(current.getId());
            }
            break;
          }
          current = idToCard.get(current.getVariantOf());
          if (current == null) {
            violations.add(new ConstraintViolation("card/"+ card.getId(), "variantOf does not point to a valid card"));
            continue variant_outer;
          }
          if (cycleDetector.contains(current.getId())) {
            violations.add(new ConstraintViolation("card/"+ card.getId(), "variantIds must not have cycles! "+cycleDetector));
            continue variant_outer;
          }
          cycleDetector.add(current.getId());
        }
      }
      if (card.getVariantType() == null) {
        card.setVariantType(!card.getVariantOf().equals(card.getId()) ? VariantType.REPRINT : VariantType.REGULAR);
      }
      variantsMap.computeIfAbsent(card.getVariantOf(), s -> new THashSet<>()).add(card);
    }

    for (Card card : idToCard.values()) {
      if (card.getId().equals(card.getCopyOf())) {
        card.setCopyOf(null);
      }
      if (card.getCopyOf() != null) {
        Card base = idToCard.get(card.getCopyOf());

        if (card.getName() != null && !Objects.equals(base.getName(), card.getName())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different name in copied card! "+ base.getName() +", "+ card.getName()));
        } else {
          card.setName(base.getName());
        }
        if (card.getRetreatCost() != null && !Objects.equals(base.getRetreatCost(), card.getRetreatCost())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different retreatCost in copied card! "+ base.getRetreatCost() +", "+ card.getRetreatCost()));
        } else {
          card.setRetreatCost(base.getRetreatCost());
        }
        if (card.getTypes() != null && !Objects.equals(base.getTypes(), card.getTypes())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different types in copied card! "+ base.getTypes() +", "+ card.getTypes()));
        } else {
          card.setTypes(base.getTypes());
        }
        if (card.getSubTypes() != null && !Objects.equals(base.getSubTypes(), card.getSubTypes())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different subTypes in copied card! "+ base.getSubTypes() +", "+ card.getSubTypes()));
        } else {
          card.setSubTypes(base.getSubTypes());
        }
        if (card.getSuperType() != null && !Objects.equals(base.getSuperType(), card.getSuperType())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different superType in copied card! "+ base.getSuperType() +", "+ card.getSuperType()));
        } else {
          card.setSuperType(base.getSuperType());
        }
        if (card.getWeaknesses() != null && !Objects.equals(base.getWeaknesses(), card.getWeaknesses())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different weaknesses in copied card! "+ base.getWeaknesses() +", "+ card.getWeaknesses()));
        } else {
          card.setWeaknesses(base.getWeaknesses());
        }
        if (card.getResistances() != null && !Objects.equals(base.getResistances(), card.getResistances())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different resistances in copied card! "+ base.getResistances() +", "+ card.getResistances()));
        } else {
          card.setResistances(base.getResistances());
        }
        if (card.getMoves() != null && !Objects.equals(base.getMoves(), card.getMoves())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different moves in copied card! "+ base.getMoves() +", "+ card.getMoves()));
        } else {
          card.setMoves(base.getMoves());
        }
        if (card.getAbilities() != null && !Objects.equals(base.getAbilities(), card.getAbilities())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different abilities in copied card! "+ base.getAbilities() +", "+ card.getAbilities()));
        } else {
          card.setAbilities(base.getAbilities());
        }
        if (card.getHp() != null && !Objects.equals(base.getHp(), card.getHp())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different hp in copied card! "+ base.getHp() +", "+ card.getHp()));
        } else {
          card.setHp(base.getHp());
        }
        if (card.getEvolvesTo() != null && !Objects.equals(base.getEvolvesTo(), card.getEvolvesTo())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different evolvesTo in copied card! "+ base.getEvolvesTo() +", "+ card.getEvolvesTo()));
        } else {
          card.setEvolvesTo(base.getEvolvesTo());
        }
        if (card.getEvolvesFrom() != null && !Objects.equals(base.getEvolvesFrom(), card.getEvolvesFrom())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different evolvesFrom in copied card! "+ base.getEvolvesFrom() +", "+ card.getEvolvesFrom()));
        } else {
          card.setEvolvesFrom(base.getEvolvesFrom());
        }
        if (card.getStage() != null && !Objects.equals(base.getStage(), card.getStage())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different stage in copied card! "+ base.getStage() +", "+ card.getStage()));
        } else {
          card.setStage(base.getStage());
        }
        if (card.getText() != null && !Objects.equals(base.getText(), card.getText())) {
//          violations.add(new ConstraintViolation("card/"+card.id, "different text in copied card! "+base.text+", "+card.text));
          // text changes between variants are fine
        } else {
          card.setText(base.getText());
        }
        if (card.getEnergy() != null && !Objects.equals(base.getEnergy(), card.getEnergy())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different energy in copied card! "+ base.getEnergy() +", "+ card.getEnergy()));
        } else {
          card.setEnergy(base.getEnergy());
        }
        if (card.getNationalPokedexNumber() != null && !Objects.equals(base.getNationalPokedexNumber(), card.getNationalPokedexNumber())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different nationalPokedexNumber in copied card! "+ base.getNationalPokedexNumber() +", "+ card.getNationalPokedexNumber()));
        } else {
          card.setNationalPokedexNumber(base.getNationalPokedexNumber());
        }
        if (card.getErratas() != null && !Objects.equals(base.getErratas(), card.getErratas())) {
          violations.add(new ConstraintViolation("card/"+ card.getId(), "different erratas in copied card! "+ base.getErratas() +", "+ card.getErratas()));
        } else {
          card.setErratas(base.getErratas());
        }
      }
    }

    assertNoViolation(violations);

    for (Expansion expansion : expansions) {
      int order = 1;

      validate(expansion, violations);
      if (!violations.isEmpty())
        continue;

      log.debug("Processed {}", expansion.getName());

      expansion.setOrder(1000 - Integer.parseInt(expansion.getId()));
      if(expansion.getSeoName() == null)
        expansion.setSeoName(expansion.getName().toLowerCase(Locale.ENGLISH).replaceAll("\\W+", "-"));

      allExpansions.add(expansion);
      idToExpansion.put(expansion.getId(), expansion);
      idToExpansion.put(expansion.getEnumId(), expansion);
      idToExpansion.put(expansion.getSeoName(), expansion);

      for (Card card : expansion.getCards()) {
        card.setExpansion(expansion);
        if(expansion.getNotImplemented() != null && expansion.getNotImplemented() && !card.getSubTypes().contains(CardType.NOT_IMPLEMENTED)) {
          card.getSubTypes().add(CardType.NOT_IMPLEMENTED);
        }
        card.setFullName(String.format("%s (%s %s)", card.getName(), card.getExpansion().getAbbr().toUpperCase(Locale.ENGLISH), card.getNumber()));
        // upgraded to uniform scan url scheme @ 09.08.2020
        card.setImageUrl(String.format("https://tcgone.net/scans/m/%s/%s.jpg", card.getExpansion().getEnumId().toLowerCase(Locale.ENGLISH), card.getNumber()));
        card.setImageUrlHiRes(String.format("https://tcgone.net/scans/l/%s/%s.jpg", card.getExpansion().getEnumId().toLowerCase(Locale.ENGLISH), card.getNumber()));
//                card.seoName = card.name.toLowerCase(Locale.ENGLISH).replaceAll("\\W","-");
//                card.seoName = String.format("%s-%s--%s", card.name.toLowerCase(Locale.ENGLISH).replaceAll("\\W+","-"), setFile.expansion.seoName, card.id);
        card.setSeoName(String.format("%s-%s-%s", card.getName().replace("é", "e").replaceAll("\\W+", "-"), expansion.getAbbr(), card.getNumber()).toLowerCase(Locale.ENGLISH));
        card.setOrder(order++);
        card.setOrder(card.getOrder() + expansion.getOrder() * 1000);
//                card.superType = Character.toTitleCase(card.superType.charAt(0)) + card.superType.substring(1).toLowerCase(Locale.ENGLISH);
        StringBuilder ftxb = new StringBuilder();
        String dlm = " • ";
        ftxb.append(card.getName()).append(dlm).append(card.getExpansion().getName()).append(" ").append(card.getNumber()).append(dlm)
          .append(card.getSuperType()).append(dlm).append(card.getSubTypes()).append(dlm).append(card.getRarity());
        if (card.getAbilities() != null) {
          for (Ability ability : card.getAbilities()) {
            ftxb.append(dlm).append(ability.getType()).append(": ").append(ability.getName()).append(dlm).append(ability.getText());
          }
        }
        if (card.getMoves() != null) {
          for (Move move : card.getMoves()) {
            ftxb.append(dlm).append(move.getName()).append(": ");
            if (move.getDamage() != null) {
              ftxb.append(move.getDamage()).append(" damage. ");
            }
            if (move.getText() != null) {
              ftxb.append(move.getText());
            }
          }
        }
        if (card.getText() != null) {
          for (String s : card.getText()) {
            ftxb.append(dlm).append(s);
          }
        }
        card.setFullText(ftxb.toString());


        pioIdToCard.put(card.getPioId(), card);
        seoNameToCard.put(card.getSeoName(), card);
        cardEnumIdToCard.put(card.getEnumId() + ":" + expansion.getEnumId(), card);
        allCards.add(card);

      } // end expansion

    } // end expansions

    assertNoViolation(violations);

    for (Collection<Card> cards : variantsMap.values()) {
      List<Variant> variants = new ArrayList<>();
      for (Card card : cards) {
        Variant variant = new Variant();
        variant.setId(card.getId());
        variant.setType(card.getVariantType());
        variant.setCopyId(card.getCopyOf() != null ? card.getCopyOf() : card.getId());
        variants.add(variant);
      }
//      if (variants.size() > 1) {
//        System.out.println(variantId+","+name+","+variants);
//      }
      for (Card card : cards) {
        card.setVariants(variants); // make it immutable?
      }
    }

    log.info("Imported {} cards", allCards.size());
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
      if (format.getFlags() != null && format.getFlags().contains("disabled")) {
        continue;
      }

      if(isBlank(format.getEnumId())) {
        violations.add(new ConstraintViolation("format/?", "enumId is missing"));
        continue;
      }
      if(isBlank(format.getSeoName())) {
        violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "seoName is missing"));
        continue;
      }
      if(isBlank(format.getName()))
        violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "name is missing"));
      if(isBlank(format.getDescription()))
        violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "description is missing"));
      if(format.getExpansions() == null || format.getExpansions().isEmpty())
        violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "expansions missing"));
      if(isBlank(format.getRuleSet()))
        violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "ruleSet is missing"));

      // expand all ranges
      Function<Stream<String>, Stream<String>> expandRanges =
        (stream) -> stream.flatMap(s -> {
          Matcher matcher = idRangePattern.matcher(s);
          if(matcher.find()){
            String startId = matcher.group(1);
            String endId = matcher.group(2);
            log.debug("Range detected: {}, start={}, end={}", s, startId, endId);
            Card startCard = idToCard.get(startId);
            if(startCard == null) {
              throw new IllegalStateException("Cannot find card " + startId);
            }
            int startIndex = startCard.getExpansion().getCards().indexOf(startCard);
            List<String> accumulator = new ArrayList<>();
            while (startIndex < startCard.getExpansion().getCards().size()) {
              Card card = startCard.getExpansion().getCards().get(startIndex);
              accumulator.add(card.getId());
              if(card.getId().equals(endId)) {
                break;
              }
              startIndex++;
            }
            log.debug("Expanded range {} to {}", s, accumulator);
            return accumulator.stream();
          } else {
            // single id, leave it
            return Stream.of(s);
          }
        });
      format.setIncludes(format.getIncludes() != null ? expandRanges.apply(format.getIncludes().stream()).collect(Collectors.toList()) : new ArrayList<>());
      format.setExcludes(format.getExcludes() != null ? expandRanges.apply(format.getExcludes().stream()).collect(Collectors.toList()) : new ArrayList<>());

      // find all cards of each format

      String id = format.getSeoName();
      LinkedHashSet<Expansion> expansions = new LinkedHashSet<>();
      LinkedHashSet<Expansion> inclusionExpansions = new LinkedHashSet<>();
      LinkedHashSet<Card> cards = new LinkedHashSet<>();


      for (String setId : format.getExpansions()) {
        Expansion expansion = idToExpansion.get(setId);
        if(expansion == null) {
          violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "expansion cannot be found "+setId));
          continue;
        }
        expansions.add(expansion);
      }

      for (String include : format.getIncludes()) {
        Card card = idToCard.get(include);
        if(card == null) {
          violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "include cannot be found "+include));
          continue;
        }
        inclusionExpansions.add(card.getExpansion());
        cards.add(card);
      }

      for (Expansion expansion : expansions) {
        if (!inclusionExpansions.contains(expansion)) {
          cards.addAll(expansion.getCards());
        }
      }

      for (String exclude : format.getExcludes()) {
        Card card = idToCard.get(exclude);
        if(card == null) {
          violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "exclude cannot be found "+exclude));
          continue;
        }
        if (inclusionExpansions.contains(card.getExpansion())) {
          violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "includes and excludes cannot be specified for cards from the expansion "+ card.getExpansion().getName()));
          continue;
        }
        if (!cards.remove(card)) {
          violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "exclude was not included at all "+exclude));
        }
      }

      // add format to each valid card

      for (Card card : cards) {
        if (card.getFormats() == null)
          card.setFormats(new ArrayList<>());

        card.getFormats().add(id);
      }

      idToFormat.put(id, format);
      idToFormat.put(format.getEnumId(), format);
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
      if (StringUtils.isNotEmpty(card.getEvolvesFrom())) {
        addToChain(card.getEvolvesFrom(), card.getName());
      }
      if (card.getSuperType() == CardType.POKEMON || card.getSubTypes().contains(CardType.FOSSIL)) {
        pokemonNames.add(card.getName());
      }
    }

    List<String> errors = new ArrayList<>();
    for (Card card : allCards) {
      if (StringUtils.isNotEmpty(card.getEvolvesFrom())) {
        if (!pokemonNames.contains(card.getEvolvesFrom())) {
          errors.add(String.format("Cannot find card %s referenced via evolvesFrom in %s %s", card.getEvolvesFrom(), card.getId(), card.getExpansion().getName()));
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
