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
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tcgone.carddb.model.*;

import java.io.*;
import java.nio.file.Files;
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
  public static final String ID_RANGE_PATTERN = "^([\\w-]+)\\.\\.([\\w-]+)$";
  private final Collection<File> inputFiles;

  protected ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

  @Getter
  protected List<EnhancedCard> allCards = new ArrayList<>();
  @Getter
  protected List<ExpansionFile> expansionFiles = new ArrayList<>();
  @Getter
  protected Map<String, EnhancedCard> idToCard = new THashMap<>();
  protected Map<String, Collection<EnhancedCard>> variantsMap = new THashMap<>();

  protected List<Format> allFormats = new ArrayList<>();
  protected Map<String, Format> idToFormat = new THashMap<>();

  protected List<Expansion> allExpansions = new ArrayList<>();
  protected Map<String, Expansion> idToExpansion = new THashMap<>();

  protected Map<String, java.util.Set<String>> chains = new THashMap<>();
  protected Pattern cardEnumIdPattern = Pattern.compile("^[A-Z0-9_]+:[A-Z0-9_]+$");

  public Importer() {
    this(null);
  }

  public Importer(Collection<File> inputFiles) {
    this.inputFiles = inputFiles;
  }

  public void process() throws ImportException {
    processExpansionsAndCards();
    processEvolutionChains();
    processFormats();
  }

  protected void processExpansionsAndCards() throws ImportException {
    // read
    readExpansionFiles();

    // setup violations
    List<ConstraintViolation> violations = new ArrayList<>();

    // validate & enhance expansion
    validateExpansions(violations);
    assertNoViolation(violations);
    enhanceExpansions();

    // validate cards (1)
    validateCardEnumIds(violations);
    assertNoViolation(violations);
    prepareThenAddAllEnhancedCards();

    // variant handling
    enhanceVariantFields(violations);
    assertNoViolation(violations);
    validateVariantsAndCopyPropertiesBetweenThem(violations);
    enhanceVariantsField();
    assertNoViolation(violations);

    // validate cards (2)
    for (EnhancedCard card : allCards) {
      validateCard(card, violations);
    }
    assertNoViolation(violations);

    // enhance cards
    for (EnhancedCard card : allCards) {
      enhanceCard(card, violations);
    }
    assertNoViolation(violations);

    // finish
    log.info("Imported {} cards", allCards.size());
  }

  private void readExpansionFiles() throws ImportException {
    try {
      if (inputFiles == null) {
        readExpansionFilesFromClasspath();
      } else {
        readExpansionFilesFromStack();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if(expansionFiles.size() < 100) {
      log.warn("There are only {} expansions imported. There should have been at least 100", expansionFiles.size());
    }
    if(expansionFiles.isEmpty()) {
      throw new ImportException("No cards were imported");
    }
  }
  private void readExpansionFilesFromClasspath() throws IOException {
    try (ScanResult scanResult = new ClassGraph().acceptPaths("cards").scan()) {
      scanResult.getResourcesWithExtension("yaml")
        .forEachInputStreamThrowingIOException((resource, inputStream) -> {
          try {
            expansionFiles.add(readExpansionFile(resource.getPath(), inputStream));
          } catch (ImportException e) {
            throw new RuntimeException(e);
          }
        });
    }
  }
  private void readExpansionFilesFromStack() throws IOException, ImportException {
    for (File inputFile : inputFiles) {
      expansionFiles.add(readExpansionFile(inputFile.getName(), Files.newInputStream(inputFile.toPath())));
    }
  }

  private ExpansionFile readExpansionFile(String resourceName, InputStream inputStream) throws IOException, ImportException {
    log.trace("Reading {}", resourceName);
    ExpansionFile expansionFile = mapper.readValue(inputStream, ExpansionFile.class);
    if (!"E3".equals(expansionFile.getSchema())) {
      throw new ImportException(resourceName + " doesn't have the expected schema");
    }
    return expansionFile;
  }

  private void validateExpansions(List<ConstraintViolation> violations) {
    for (ExpansionFile expansionFile : expansionFiles) {
      Expansion expansion = expansionFile.getExpansion();

      if (StringUtils.isBlank(expansion.getOrderId()) || !expansion.getOrderId().matches("^[0-9]{3}$"))
        violations.add(new ConstraintViolation("expansion/"+ expansion.getEnumId(), "expansion.orderId missing or not acceptable, they are three digits numbers to set global ordering"));
      if (StringUtils.isBlank(expansion.getName()))
        violations.add(new ConstraintViolation("expansion/"+ expansion.getEnumId(), "expansion.name missing"));
      if (StringUtils.isBlank(expansion.getEnumId()))
        violations.add(new ConstraintViolation("expansion/"+ expansion.getEnumId(), "expansion.enumId missing"));
      if (StringUtils.isBlank(expansion.getShortName()))
        violations.add(new ConstraintViolation("expansion/"+ expansion.getEnumId(), "expansion.shortName missing"));
      if (expansionFile.getCards() == null || expansionFile.getCards().isEmpty())
        violations.add(new ConstraintViolation("expansion/"+ expansion.getEnumId(), "cards missing"));
    }
  }

  private void assertNoViolation(List<ConstraintViolation> violations) throws ImportException {
    if (!violations.isEmpty()) {
      throw new ImportException("Validation failed", violations);
    }
  }


  private void enhanceExpansions() {
    for (ExpansionFile expansionFile : expansionFiles) {
      Expansion expansion = expansionFile.getExpansion();
      log.debug("Enhancing {}", expansion.getName());
      if(expansion.getSeoName() == null)
        expansion.setSeoName(expansion.getName().toLowerCase(Locale.ENGLISH).replaceAll("\\W+", "-"));

      allExpansions.add(expansion);
      idToExpansion.put(expansion.getEnumId(), expansion);
      idToExpansion.put(expansion.getEnumId(), expansion);
      idToExpansion.put(expansion.getSeoName(), expansion);
    }
  }

  private void validateCardEnumIds(List<ConstraintViolation> violations) {
    for (ExpansionFile expansionFile : expansionFiles) {
      Expansion expansion = expansionFile.getExpansion();
      for (Card card : expansionFile.getCards()) {
        if (StringUtils.isBlank(card.getEnumId())) {
          violations.add(new ConstraintViolation("cards/"+ expansion.getEnumId() +"/card/?", "enumId missing"));
        } else if (!cardEnumIdPattern.matcher(card.getEnumId()).matches()) {
          violations.add(new ConstraintViolation(getContextFor(expansion, card), "enumId must match NAME_NUMBER:EXPANSION"));
        } else if (!card.getEnumId().endsWith(":" + expansion.getEnumId())) {
          violations.add(new ConstraintViolation(getContextFor(expansion, card), "enumId must match NAME_NUMBER:EXPANSION thus must end with " + expansion.getEnumId()));
        }
      }
    }
  }

  private void prepareThenAddAllEnhancedCards() {
    for (ExpansionFile expansionFile : expansionFiles) {
      Expansion expansion = expansionFile.getExpansion();
      for (Card card : expansionFile.getCards()) {
        EnhancedCard enhancedCard = EnhancedCard.fromCard(card);
        enhancedCard.setExpansion(expansion);
        allCards.add(enhancedCard);
        idToCard.put(card.getEnumId(), enhancedCard);
      }
    }
  }

  private void validateCard(EnhancedCard card, List<ConstraintViolation> violations) {

    String context = getContextFor(card);

    if (StringUtils.isBlank(card.getName()))
      violations.add(new ConstraintViolation(context, "name missing"));
    if (StringUtils.isBlank(card.getNumber()))
      violations.add(new ConstraintViolation(context, "number missing"));
    if (StringUtils.isBlank(card.getEnumId()))
      violations.add(new ConstraintViolation(context, "enumId missing"));
    if (card.getSuperType() == null || !card.getSuperType().isSuperType())
      violations.add(new ConstraintViolation(context, "superType missing or illegal"));
    if (card.getRarity() == null)
      violations.add(new ConstraintViolation(context, "rarity missing"));

    if (card.getCardTypes().contains(CardType.POKEMON)) {
      if (card.getHp() == null && !card.getCardTypes().contains(CardType.LEGEND))
        violations.add(new ConstraintViolation(context, "hp missing"));
      if (card.getRetreatCost() == null && !card.getCardTypes().contains(CardType.LEGEND))
        violations.add(new ConstraintViolation(context, "retreatCost missing"));
      if (card.getCardTypes().contains(CardType.STAGE1) || card.getCardTypes().contains(CardType.STAGE2)) {
        if(card.getEvolvesFrom() != null && !card.getEvolvesFrom().isEmpty()) {
          violations.add(new ConstraintViolation(context, "evolvesFrom missing"));
        }
      }
      if (card.getAbilities() != null)
        for (Ability ability : card.getAbilities()) {
          if (StringUtils.isBlank(ability.getName()) || StringUtils.isBlank(ability.getType()) || StringUtils.isBlank(ability.getText()))
            violations.add(new ConstraintViolation(context +"/abilities", "name, type or text missing"));
        }
      if (card.getMoves() != null)
        for (Move move : card.getMoves()) {
          if (StringUtils.isBlank(move.getName()))
            violations.add(new ConstraintViolation(context +"/moves", "name missing"));
          if (move.getCost() == null)
            violations.add(new ConstraintViolation(context +"/moves", "cost missing"));
        }
      if (card.getWeaknesses() != null)
        for (WeaknessResistance wr : card.getWeaknesses()) {
          if (StringUtils.isBlank(wr.getValue()) || wr.getType() == null)
            violations.add(new ConstraintViolation(context +"/weaknesses", "value or type missing"));
        }
      if (card.getResistances() != null)
        for (WeaknessResistance wr : card.getResistances()) {
          if (StringUtils.isBlank(wr.getValue()) || wr.getType() == null)
            violations.add(new ConstraintViolation(context +"/resistances", "value or type missing"));
        }

      findAndValidateStage(card, violations, context);

      // TODO
      // check sub types
      // check empty/null fields, number, ordering, etc, attacks, abilities
    }

  }

  private static CardType findAndValidateStage(EnhancedCard card, List<ConstraintViolation> violations, String context) {
    // stage handling
    CardType stage = null;
    for (CardType cardType : CardType.allStages()) {
      if (card.getCardTypes().contains(cardType)) {
        if (stage != null) {
          if ((stage == CardType.BASIC && cardType == CardType.BABY) || stage == CardType.BABY && cardType == CardType.BASIC) {
            stage = CardType.BABY; // this is fine
          } else {
            violations.add(new ConstraintViolation(context +"/subTypes", String.format("cannot have both: %s, %s", stage, cardType)));
          }
        } else {
          stage = cardType;
        }
      }
    }
    if (stage == null) {
      violations.add(new ConstraintViolation(context +"/subTypes", String.format("must have one: %s", CardType.allStages())));
    }
    return stage;
  }

  private static String getContextFor(EnhancedCard card) {
    return "cards/" + card.getExpansion().generateFileName() + "/" + card.getEnumId();
  }

  private static String getContextFor(Expansion expansion, Card card) {
    return "cards/" + expansion.generateFileName() + "/" + card.getEnumId();
  }

  private void validateVariantsAndCopyPropertiesBetweenThem(List<ConstraintViolation> violations) {
    for (EnhancedCard card : allCards) {
      if (Boolean.TRUE.equals(card.getExpansion().getIsFanMade())) // don't care about fakes
        continue;

      if (card.getEnumId().equals(card.getCopyOf())) {
        card.setCopyOf(null);
      }
      String context = getContextFor(card);

      if (card.getCopyOf() != null) {
        EnhancedCard base = idToCard.get(card.getCopyOf());

        if (card.getName() != null && !Objects.equals(base.getName(), card.getName())) {
          violations.add(new ConstraintViolation(context, "different name in copied card! "+ base.getName() +", "+ card.getName()));
        } else {
          card.setName(base.getName());
        }
        if (card.getRetreatCost() != null && !Objects.equals(base.getRetreatCost(), card.getRetreatCost())) {
          violations.add(new ConstraintViolation(context, "different retreatCost in copied card! "+ base.getRetreatCost() +", "+ card.getRetreatCost()));
        } else {
          card.setRetreatCost(base.getRetreatCost());
        }
        if (card.getTypes() != null && !Objects.equals(base.getTypes(), card.getTypes())) {
          violations.add(new ConstraintViolation(context, "different types in copied card! "+ base.getTypes() +", "+ card.getTypes()));
        } else {
          card.setTypes(base.getTypes());
        }
        if (card.getCardTypes() != null && !Objects.equals(base.getCardTypes(), card.getCardTypes())) {
          violations.add(new ConstraintViolation(context, "different cardTypes in copied card! "+ base.getCardTypes() +", "+ card.getCardTypes()));
        } else {
          card.setCardTypes(base.getCardTypes());
        }
        if (card.getWeaknesses() != null && !Objects.equals(base.getWeaknesses(), card.getWeaknesses())) {
          violations.add(new ConstraintViolation(context, "different weaknesses in copied card! "+ base.getWeaknesses() +", "+ card.getWeaknesses()));
        } else {
          card.setWeaknesses(base.getWeaknesses());
        }
        if (card.getResistances() != null && !Objects.equals(base.getResistances(), card.getResistances())) {
          violations.add(new ConstraintViolation(context, "different resistances in copied card! "+ base.getResistances() +", "+ card.getResistances()));
        } else {
          card.setResistances(base.getResistances());
        }
        if (card.getMoves() != null && !Objects.equals(base.getMoves(), card.getMoves())) {
          violations.add(new ConstraintViolation(context, "different moves in copied card! "+ base.getMoves() +", "+ card.getMoves()).setTask(new Task(base, card, MergeField.MOVES)));
        } else {
          card.setMoves(base.getMoves());
        }
        if (card.getAbilities() != null && !Objects.equals(base.getAbilities(), card.getAbilities())) {
          violations.add(new ConstraintViolation(context, "different abilities in copied card! "+ base.getAbilities() +", "+ card.getAbilities()).setTask(new Task(base, card, MergeField.ABILITIES)));
        } else {
          card.setAbilities(base.getAbilities());
        }
        if (card.getHp() != null && !Objects.equals(base.getHp(), card.getHp())) {
          violations.add(new ConstraintViolation(context, "different hp in copied card! "+ base.getHp() +", "+ card.getHp()));
        } else {
          card.setHp(base.getHp());
        }
        if (card.getEvolvesTo() != null && !Objects.equals(base.getEvolvesTo(), card.getEvolvesTo())) {
          violations.add(new ConstraintViolation(context, "different evolvesTo in copied card! "+ base.getEvolvesTo() +", "+ card.getEvolvesTo()));
        } else {
          card.setEvolvesTo(base.getEvolvesTo());
        }
        if (card.getEvolvesFrom() != null && !Objects.equals(base.getEvolvesFrom(), card.getEvolvesFrom())) {
          violations.add(new ConstraintViolation(context, "different evolvesFrom in copied card! "+ base.getEvolvesFrom() +", "+ card.getEvolvesFrom()));
        } else {
          card.setEvolvesFrom(base.getEvolvesFrom());
        }
        if (card.getStage() != null && !Objects.equals(base.getStage(), card.getStage())) {
          violations.add(new ConstraintViolation(context, "different stage in copied card! "+ base.getStage() +", "+ card.getStage()));
        } else {
          card.setStage(base.getStage());
        }
        if (card.getText() != null && !Objects.equals(base.getText(), card.getText())) {
          // text changes between variants are fine
          violations.add(new ConstraintViolation(context, "different text in copied card! "+ base.getText() +", "+ card.getText()).setTask(new Task(base, card, MergeField.TEXT)));
        } else {
          card.setText(base.getText());
        }
        if (card.getEnergy() != null && !Objects.equals(base.getEnergy(), card.getEnergy())) {
          violations.add(new ConstraintViolation(context, "different energy in copied card! "+ base.getEnergy() +", "+ card.getEnergy()));
        } else {
          card.setEnergy(base.getEnergy());
        }
        if (card.getNationalPokedexNumber() != null && !Objects.equals(base.getNationalPokedexNumber(), card.getNationalPokedexNumber())) {
          violations.add(new ConstraintViolation(context, "different nationalPokedexNumber in copied card! "+ base.getNationalPokedexNumber() +", "+ card.getNationalPokedexNumber()));
        } else {
          card.setNationalPokedexNumber(base.getNationalPokedexNumber());
        }
        if (card.getErratas() != null && !Objects.equals(base.getErratas(), card.getErratas())) {
          violations.add(new ConstraintViolation(context, "different erratas in copied card! "+ base.getErratas() +", "+ card.getErratas()));
        } else {
          card.setErratas(base.getErratas());
        }
      }
    }
  }

  private void enhanceVariantFields(List<ConstraintViolation> violations) {
    variant_outer:
    for (final EnhancedCard card : allCards) {
      String context = getContextFor(card);

      if (card.getVariantOf() == null) {
        card.setVariantOf(card.getEnumId());
      }
      if (!card.getVariantOf().equals(card.getEnumId())) {
        THashSet<String> cycleDetector = new THashSet<>();
        Card current = card;
        cycleDetector.add(current.getEnumId());
        while (true) {
          if (Boolean.TRUE.equals(current.getVariantIsDifferent()) && card.getCopyOf() == null) {
            card.setCopyOf(current.getEnumId());
          }
          if (current.getEnumId().equals(current.getVariantOf()) || current.getVariantOf() == null) {
            if (Boolean.TRUE.equals(current.getVariantIsDifferent())) {
              violations.add(new ConstraintViolation(context, "when variantIsDifferent is TRUE, variantOf must always point to a different variant! "));
              continue variant_outer;
            }
            card.setVariantOf(current.getEnumId());
            if (card.getCopyOf() == null) {
              card.setCopyOf(current.getEnumId());
            }
            break;
          }
          current = idToCard.get(current.getVariantOf());
          if (current == null) {
            violations.add(new ConstraintViolation(context, "variantOf does not point to a valid card"));
            continue variant_outer;
          }
          if (cycleDetector.contains(current.getEnumId())) {
            violations.add(new ConstraintViolation(context, "variantIds must not have cycles! "+cycleDetector));
            continue variant_outer;
          }
          cycleDetector.add(current.getEnumId());
        }
      }
      if (card.getVariantType() == null) {
        card.setVariantType(!card.getVariantOf().equals(card.getEnumId()) ? VariantType.REPRINT : VariantType.REGULAR);
      }
      variantsMap.computeIfAbsent(card.getVariantOf(), s -> new THashSet<>()).add(card);
    }
  }

  private void enhanceVariantsField() {
    for (Collection<EnhancedCard> cards : variantsMap.values()) {
      List<Variant> variants = new ArrayList<>();
      for (EnhancedCard card : cards) {
        Variant variant = new Variant();
        variant.setId(card.getEnumId());
        variant.setType(card.getVariantType());
        variant.setCopyId(card.getCopyOf() != null ? card.getCopyOf() : card.getEnumId());
        variants.add(variant);
        card.setVariants(variants);
      }
    }
  }

  private static void enhanceCard(EnhancedCard card, List<ConstraintViolation> violations) {
    // stage
    CardType stage = findAndValidateStage(card, violations, getContextFor(card));
    if (stage == CardType.BABY && !card.getCardTypes().contains(CardType.BASIC)) {
      card.getCardTypes().add(CardType.BASIC);
    }
    card.setStage(stage);

    // check not implemented
    if(Boolean.TRUE.equals(card.getExpansion().getNotImplemented()) && !card.getCardTypes().contains(CardType.NOT_IMPLEMENTED)) {
      card.getCardTypes().add(CardType.NOT_IMPLEMENTED);
    }

    // fullName
    card.setFullNameV1(String.format("%s (%s %s)", card.getName(), card.getExpansion().getShortName(), card.getNumber()));
    card.setFullName(String.format("%s %s %s", card.getName(), card.getExpansion().getShortName(), card.getNumber()));

    // scanUrl -- large only
    card.setScanUrl(String.format("https://tcgone.net/scans/l/%s/%s.jpg", card.getExpansion().getEnumId().toLowerCase(Locale.ENGLISH), card.getNumber()));

    // seoName
//      card.seoName = card.name.toLowerCase(Locale.ENGLISH).replaceAll("\\W","-");
//      card.seoName = String.format("%s-%s--%s", card.name.toLowerCase(Locale.ENGLISH).replaceAll("\\W+","-"), setFile.expansion.seoName, card.id);
    card.setSeoName(String.format("%s-%s-%s", card.getName().replace("é", "e").replaceAll("\\W+", "-"), card.getExpansion().getShortName(), card.getNumber()).toLowerCase(Locale.ENGLISH));

    // superType
    if (card.getCardTypes().contains(CardType.POKEMON)) {
      card.setSuperType(CardType.POKEMON);
    }
    else if (card.getCardTypes().contains(CardType.ENERGY)) {
      card.setSuperType(CardType.ENERGY);
    }
    else {
      card.setSuperType(CardType.TRAINER);
    }

    // subtypes
    List<CardType> subTypes = new ArrayList<>();
    for (CardType cardType : card.getCardTypes()) {
      if (cardType != card.getSuperType()) {
        subTypes.add(cardType);
      }
    }
    card.setSubTypes(subTypes);

    // fullText
    StringBuilder ftxb = new StringBuilder();
    String dlm = " • ";
    ftxb.append(card.getName()).append(dlm).append(card.getExpansion().getName()).append(" ").append(card.getNumber()).append(dlm)
      .append(card.getSuperType()).append(dlm).append(card.getCardTypes()).append(dlm).append(card.getRarity());
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
      ftxb.append(dlm).append(card.getText());
    }
    card.setFullText(ftxb.toString());

    // TODO enhance evolvesTo fields

  }


  protected void processFormats() throws ImportException {

    List<Format> formatsFromFile = null;
    try {
      formatsFromFile = mapper.readValue(getClass().getClassLoader().getResourceAsStream("formats.yaml"), new TypeReference<List<Format>>() {});
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Pattern idRangePattern = Pattern.compile(ID_RANGE_PATTERN);

    List<ConstraintViolation> violations = new ArrayList<>();

    for (Format format : formatsFromFile) {
      if (format.getFlags() != null && format.getFlags().contains("disabled")) {
        continue;
      }

      if(StringUtils.isBlank(format.getEnumId())) {
        violations.add(new ConstraintViolation("format/?", "enumId is missing"));
        continue;
      }
      if(StringUtils.isBlank(format.getSeoName())) {
        violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "seoName is missing"));
        continue;
      }
      if(StringUtils.isBlank(format.getName()))
        violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "name is missing"));
      if(StringUtils.isBlank(format.getDescription()))
        violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "description is missing"));
      if(format.getExpansions() == null || format.getExpansions().isEmpty())
        violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "expansions missing"));
      if(StringUtils.isBlank(format.getRuleSet()))
        violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "ruleSet is missing"));

      // expand all ranges
      Function<Stream<String>, Stream<String>> expandRanges =
        (stream) -> stream.flatMap(s -> {
          Matcher matcher = idRangePattern.matcher(s);
          if(matcher.find()){
            String startId = matcher.group(1);
            String endId = matcher.group(2);
            log.debug("Range detected: {}, start={}, end={}", s, startId, endId);
            EnhancedCard startCard = idToCard.get(startId);
            if(startCard == null) {
              throw new IllegalStateException("Cannot find card " + startId);
            }
            List<EnhancedCard> cardsOfExpansion = startCard.findCardsOfExpansion(allCards);
            int startIndex = cardsOfExpansion.indexOf(startCard);
            List<String> accumulator = new ArrayList<>();
            while (startIndex < cardsOfExpansion.size()) {
              Card card = cardsOfExpansion.get(startIndex);
              accumulator.add(card.getEnumId());
              if(card.getEnumId().equals(endId)) {
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
      LinkedHashSet<EnhancedCard> cards = new LinkedHashSet<>();


      for (String setId : format.getExpansions()) {
        Expansion expansion = idToExpansion.get(setId);
        if(expansion == null) {
          violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "expansion cannot be found "+setId));
          continue;
        }
        expansions.add(expansion);
      }

      for (String include : format.getIncludes()) {
        EnhancedCard card = idToCard.get(include);
        if(card == null) {
          violations.add(new ConstraintViolation("format/"+ format.getEnumId(), "include cannot be found "+include));
          continue;
        }
        inclusionExpansions.add(card.getExpansion());
        cards.add(card);
      }

      for (Expansion expansion : expansions) {
        if (!inclusionExpansions.contains(expansion)) {
          cards.addAll(allCards.stream().filter(card -> card.getExpansion().equals(expansion)).collect(Collectors.toList()));
        }
      }

      for (String exclude : format.getExcludes()) {
        EnhancedCard card = idToCard.get(exclude);
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

      for (EnhancedCard card : cards) {
        if (card.getLegalInFormats() == null)
          card.setLegalInFormats(new ArrayList<>());

        card.getLegalInFormats().add(id);
      }

      idToFormat.put(id, format);
      idToFormat.put(format.getEnumId(), format);
      allFormats.add(format);
    }

    if (!violations.isEmpty()) {
      throw new ImportException("Validation failure while importing formats", violations);
    }

  }

  protected void processEvolutionChains() throws ImportException {
    chains = new THashMap<>();
    HashSet<String> pokemonNames = new HashSet<>();

    for (EnhancedCard card : allCards) {
      if (card.getEvolvesFrom() != null && !card.getEvolvesFrom().isEmpty()) {
        for (String evolvesFrom : card.getEvolvesFrom()) {
          addToChain(evolvesFrom, card.getName());
        }
      }
      if (card.getCardTypes().contains(CardType.POKEMON) || card.getCardTypes().contains(CardType.FOSSIL)) {
        pokemonNames.add(card.getName());
      }
    }

    List<String> errors = new ArrayList<>();
    for (EnhancedCard card : allCards) {
      if (card.getEvolvesFrom() != null && !card.getEvolvesFrom().isEmpty()) {
        for (String evolvesFrom : card.getEvolvesFrom()) {
          if (!pokemonNames.contains(evolvesFrom)) {
            errors.add(String.format("Cannot find card %s referenced via evolvesFrom in %s %s", evolvesFrom, card.getEnumId(), card.getExpansion().getName()));
          }
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

  public List<EnhancedCard> findCardsOfExpansion(Expansion expansion) {
    return allCards
      .stream()
      .filter(card -> card.getExpansion().equals(expansion))
      .collect(Collectors.toList());
  }

}
