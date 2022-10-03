package tcgone.carddb.tools.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import tcgone.carddb.model.Type;
import tcgone.carddb.model.WeaknessResistance;

import java.io.IOException;

import static tcgone.carddb.tools.deserializers.DeserializerUtils.sanitizeCross;

public class WeaknessResistanceDeserializer  extends StdDeserializer<WeaknessResistance> {
  private static final long serialVersionUID = 1L;

  public WeaknessResistanceDeserializer() {
    this(null);
  }

  public WeaknessResistanceDeserializer(Class cls) {
    super(cls);
  }

  @Override
  public WeaknessResistance deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    WeaknessResistance result = new WeaknessResistance();
    JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
    JsonParser typeParser = jsonNode.findValue("type").traverse();
    typeParser.setCodec(jsonParser.getCodec());
    result.type = typeParser.readValueAs(Type.class);
    result.value = sanitizeCross(jsonNode.get("value").asText());
    return result;
  }
}
