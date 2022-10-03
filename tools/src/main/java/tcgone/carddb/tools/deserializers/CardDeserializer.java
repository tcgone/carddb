package tcgone.carddb.tools.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import tcgone.carddb.model.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CardDeserializer extends StdDeserializer<Card> {
  private static final List<String> superTypes = Arrays.asList(CardType.POKEMON.getDisplayLabel(), CardType.TRAINER.getDisplayLabel(), CardType.ENERGY.getDisplayLabel());

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CardDeserializer.class);

  public CardDeserializer() {
    this(null);
  }

  public CardDeserializer(Class cls) {
    super(cls);
  }

  @Override
  public Card deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    Card card = new Card();
    card.id = "FILL_THIS";

    JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);

    card.pioId = jsonNode.get("id").asText();
    card.name = jsonNode.get("name").asText();
    card.number = jsonNode.get("number").asText();
    card.enumId = formatEnumId(card.name, card.number);
    if (jsonNode.has("regulationMark")) {
      card.regulationMark = jsonNode.get("regulationMark").asText();
    }

    List<String> text = new ArrayList<>();
    if (jsonNode.has("text")) {
      for (JsonNode node : jsonNode.findValue("text")) {
        text.add(node.asText());
      }
    }

    if (jsonNode.has("rules")) {
      for (JsonNode node : jsonNode.findValue("rules")) {
        text.add(node.asText());
      }
    }

    if (!text.isEmpty()) {
      card.text = new ArrayList<>();
      card.text.addAll(text.stream().map(DeserializerUtils::replaceTypesWithShortForms).flatMap(x -> Arrays.stream(x.split("\\\n"))).filter(s -> !s.trim().isEmpty()).collect(Collectors.toList()));
    }

    Optional<Rarity> rarity = DeserializerUtils.parseNestedObject(jsonParser, jsonNode, "rarity", Rarity.class);
    rarity.ifPresent(r -> card.rarity = r);
    if (card.rarity == null) {
      card.rarity = Rarity.COMMON; // A few sets like Southern Islands and Rumble do not have rarities
    }

    Optional<CardType> superType = DeserializerUtils.parseNestedObject(jsonParser, jsonNode, "supertype", CardType.class);
    superType.ifPresent(st -> card.superType = st);

    if (card.superType == CardType.ENERGY) {
      card.subTypes = new ArrayList<>();
      if (card.text != null && !card.text.isEmpty()) {
        card.subTypes.add(CardType.SPECIAL_ENERGY);
      } else {
        card.subTypes.add(CardType.BASIC_ENERGY);
        card.energy = new ArrayList<>(Collections.singletonList(sanitizeType(Collections.singletonList(card.name.split(" ")[0]))));
      }
    } else {
      List<String> subTypes = new ArrayList<>();
      if (jsonNode.has("subtypes")) { // Pokemon.io
        for (JsonNode node : jsonNode.findValue("subtypes")) {
          subTypes.add(node.asText());
        }
      } else if (jsonNode.has("subtype")) { // Kirby
        subTypes.add(jsonNode.findValue("subtype").asText());
      }
      card.subTypes = setSubTypes(card.name, card.superType, subTypes);
    }

    if (card.superType == CardType.POKEMON) {
      // HP of one side of LEGEND cards is null
      if (jsonNode.has("hp")) {
        card.hp = jsonNode.get("hp").asInt();
      }

      if (jsonNode.has("convertedRetreatCost")) {
        card.retreatCost = jsonNode.get("convertedRetreatCost").asInt();
      }

      Optional<List<WeaknessResistance>> resistances = DeserializerUtils.parseNestedObject(jsonParser, jsonNode, "resistances", new TypeReference<List<WeaknessResistance>>(){});
      resistances.ifPresent(r -> card.resistances = r);

      Optional<List<WeaknessResistance>> weaknesses = DeserializerUtils.parseNestedObject(jsonParser, jsonNode, "weaknesses", new TypeReference<List<WeaknessResistance>>(){});
      weaknesses.ifPresent(w -> card.weaknesses = w);

      Optional<List<Move>> attacks = DeserializerUtils.parseNestedObject(jsonParser, jsonNode, "attacks", new TypeReference<List<Move>>(){});
      attacks.ifPresent(a -> card.moves = a);

      Optional<List<Ability>> abilities = DeserializerUtils.parseNestedObject(jsonParser, jsonNode, "abilities", new TypeReference<List<Ability>>(){});
      abilities.ifPresent(a -> {
        card.abilities = new ArrayList<>();
        card.abilities.addAll(a);
      });

      Optional<Ability> ancientTrait = DeserializerUtils.parseNestedObject(jsonParser, jsonNode, "ancientTrait", Ability.class);
      ancientTrait.ifPresent(a -> card.abilities.add(a));

      Optional<List<Type>> types = DeserializerUtils.parseNestedObject(jsonParser, jsonNode, "types", new TypeReference<List<Type>>(){});
      types.ifPresent(t -> card.types = t);
      if (!types.isPresent()) {
        log.warn(String.format("NULL TYPES for %s, %s", card.pioId, card.name));
      }

      if (jsonNode.has("nationalPokedexNumbers")) {
        card.nationalPokedexNumber = jsonNode.get("nationalPokedexNumbers").get(0).asInt();
      }

      if (jsonNode.has("evolvesFrom")) {
        List<String> evolvesFrom = new ArrayList<>();
        for (JsonNode node : jsonNode.findValue("evolvesFrom")) {
          evolvesFrom.add(node.asText());
        }
        if (!evolvesFrom.isEmpty()) {
          card.evolvesFrom = evolvesFrom.get(0);
        }
      }

      Optional<List<String>> evolvesTo = DeserializerUtils.parseNestedObject(jsonParser, jsonNode, "evolvesTo", new TypeReference<List<String>>(){});
      evolvesTo.ifPresent(e -> card.evolvesTo = e);
    }

    return card;
  }

  private List<Type> sanitizeType(List<String> types){
    if(types==null) return null;
    return types.stream().map(Type::of).collect(Collectors.toList());
  }

  private String formatEnumId(String name, String number) {
    return String.format("%s_%s", name
      .replace("–", "-").replace("’", "'").toUpperCase(Locale.ENGLISH)
      .replaceAll("[ \\p{Punct}]", "_").replaceAll("_+", "_").replace("É", "E"), number);
  }

  private List<CardType> setSubTypes(String name, CardType superType, List<String> subTypes) {
    List<CardType> subtypes = new ArrayList<>();

    for(String subtype : subTypes) {
      switch(subtype) {

        case "LEGEND":
          subtypes.add(CardType.LEGEND);
          break;

        case "Basic":
          if (superType == CardType.ENERGY) break;
          subtypes.add(CardType.BASIC);

          if (name.endsWith("V")) {
            subtypes.add(CardType.POKEMON_V);
          }
          break;

        case "Stage 1":
          subtypes.add(CardType.STAGE1);
          subtypes.add(CardType.EVOLUTION);
          break;

        case "Stage 2":
          subtypes.add(CardType.STAGE2);
          subtypes.add(CardType.EVOLUTION);
          break;

        case "EX":
          subtypes.add(
            name.endsWith(" ex") ? CardType.EX :
              CardType.POKEMON_EX
          );
          break;

        case "Level-Up":
          subtypes.add(CardType.EVOLUTION);
          subtypes.add(CardType.LVL_X);
          break;

        case "Restored":
          subtypes.add(CardType.RESTORED);
          break;

        case "MEGA":
          subtypes.add(CardType.MEGA_POKEMON);
          subtypes.add(CardType.POKEMON_EX);
          subtypes.add(CardType.EVOLUTION);
          break;

        case "BREAK":
          subtypes.add(CardType.BREAK);
          subtypes.add(CardType.EVOLUTION);
          break;

        case "GX":
          subtypes.add(CardType.POKEMON_GX);
          break;

        case "TAG TEAM":
          subtypes.add(CardType.TAG_TEAM);
          break;

        case "VMAX":
          subtypes.add(CardType.VMAX);
          subtypes.add(CardType.POKEMON_V);
          subtypes.add(CardType.EVOLUTION);
          break;

        case "VSTAR":
          subtypes.add(CardType.VSTAR);
          subtypes.add(CardType.POKEMON_V);
          subtypes.add(CardType.EVOLUTION);
          break;

        case "V-UNION":
          subtypes.add(CardType.V_UNION);
          break;

        case "Single Strike":
          subtypes.add(CardType.SINGLE_STRIKE);
          break;

        case "Rapid Strike":
          subtypes.add(CardType.RAPID_STRIKE);
          break;

        case "Fusion Strike":
          subtypes.add(CardType.FUSION_STRIKE);
          break;

        case "Item":
          subtypes.add(CardType.ITEM);
          break;

        case "Pokémon Tool":
          subtypes.add(CardType.POKEMON_TOOL);
          subtypes.add(CardType.ITEM);
          break;

        case "Stadium":
          subtypes.add(CardType.STADIUM);
          break;

        case "Supporter":
          subtypes.add(CardType.SUPPORTER);
          break;

        case "Technical Machine":
          subtypes.add(CardType.TECHNICAL_MACHINE);
          break;

        case "Rocket's Secret Machine":
          subtypes.add(CardType.ROCKETS_SECRET_MACHINE);
          break;
      }
    }

    Collections.sort(subtypes);
    return subtypes;
  }
}
