package tcgone.carddb.tools.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import tcgone.carddb.model.CardType;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class CardSuperTypeDeserializer extends StdDeserializer<CardType> {
  private static final long serialVersionUID = 1L;
  private static final List<String> superTypes = Arrays.asList(CardType.POKEMON.getDisplayLabel(), CardType.TRAINER.getDisplayLabel(), CardType.ENERGY.getDisplayLabel());

  public CardSuperTypeDeserializer() {
    this(null);
  }

  public CardSuperTypeDeserializer(Class cls) {
    super(cls);
  }

  @Override
  public CardType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
    String rawCardType = jsonNode.asText();
    switch (rawCardType) {
      case "Pokémon":
        return CardType.POKEMON;
      case "Trainer":
        return CardType.TRAINER;
      case "Energy":
        return CardType.ENERGY;
    }
    throw new IOException(String.format("Unknown CardType (%s) Known CardTypes: %s", rawCardType, superTypes.stream().collect(joining(") (", "(", ")"))));
  }
}
