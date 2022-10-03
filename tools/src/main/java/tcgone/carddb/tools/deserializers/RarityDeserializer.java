package tcgone.carddb.tools.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang.WordUtils;
import tcgone.carddb.model.Rarity;

import java.io.IOException;
import java.util.Locale;

public class RarityDeserializer extends StdDeserializer<Rarity> {
  private static final long serialVersionUID = 1L;

  public RarityDeserializer() {
    this(null);
  }

  public RarityDeserializer(Class cls) {
    super(cls);
  }

  @Override
  public Rarity deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
    String rawRarity = jsonNode.asText();
    return Rarity.of(WordUtils.capitalizeFully(cleanRarity(rawRarity)));
  }

  private String cleanRarity(String rawRarity) {
      return rawRarity.toLowerCase(Locale.ENGLISH)
      .replace("rare secret","Secret")
      .replace("rare ace","Rare")
      .replace("rare holo lv.x","Rare Holo")
      .replace("rare ultra","Ultra Rare")
      .replace("rareultra","Ultra Rare")
      .replace("rare prime","Rare")
      .replace("rare break","Ultra Rare")
      .replace("rare holo ex","Ultra Rare")
      .replace("rare holo gx","Ultra Rare")
      .replace("rare promo","Promo")
      .replace("legend","Ultra Rare")
      .replace("rareholovmax", "Rare Holo")
      .replace("rareholov","Rare Holo")
      .replace("rare holo vstar", "Rare Holo")
      .replace("rare holo vmax", "Rare Holo")
      .replace("rare holo v", "Rare Holo")
      .replace("rare rainbow", "Ultra Rare")
      .replace("amazing rare", "Rare Holo")
      .replace("radiant rare", "Rare")
      .replace("rare shiny", "Rare Holo")
      .replace("classic collection", "Ultra Rare")
      .replace("vm", "Rare Holo")
      .replace("v", "Rare Holo");
  }
}
