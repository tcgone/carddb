package tcgone.carddb.tools.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import tcgone.carddb.model.Ability;

import java.io.IOException;

public class AbilityDeserializer  extends StdDeserializer<Ability> {
  private static final long serialVersionUID = 1L;

  public AbilityDeserializer() {
    this(null);
  }

  public AbilityDeserializer(Class cls) {
    super(cls);
  }

  @Override
  public Ability deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    Ability result = new Ability();
    JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
    if (jsonNode.has("type")) {
      result.type = jsonNode.get("type").asText();
    } else { // Is an Ancient Trait
      result.type = "Ancient Trait";
    }
    result.name = jsonNode.get("name").asText();
    result.text = DeserializerUtils.replaceTypesWithShortForms(jsonNode.get("text").asText());
    return result;
  }
}
