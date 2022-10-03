package tcgone.carddb.tools.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import tcgone.carddb.model.Move;
import tcgone.carddb.model.Type;

import java.io.IOException;
import java.util.ArrayList;

public class MoveDeserializer extends StdDeserializer<Move> {
  private static final long serialVersionUID = 1L;

  public MoveDeserializer() {
    this(null);
  }

  public MoveDeserializer(Class cls) {
    super(cls);
  }

  @Override
  public Move deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    Move result = new Move();
    JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
    result.name = jsonNode.get("name").asText();
    if (jsonNode.has("text")) {
      result.text = DeserializerUtils.replaceTypesWithShortForms(jsonNode.get("text").asText());
    }
    if (jsonNode.has("damage")) {
      String damage = jsonNode.findValue("damage").asText();
      if (!damage.isEmpty()) {
        result.damage = DeserializerUtils.sanitizeCross(jsonNode.get("damage").asText());
      }
    }
    if (jsonNode.has("cost")) {
      result.cost = new ArrayList<>();
      for (JsonNode node : jsonNode.findValue("cost")) {
        if (node.asText().equals("[-]")) continue;
        result.cost.add(Type.of(node.asText()));
      }
    }
    return result;
  }
}
