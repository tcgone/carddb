package tcgone.carddb.tools.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Optional;

public class DeserializerUtils {

  public static String sanitizeCross(String s){
    if(s==null)return null;
    return s.replace("×","x");
  }

  public static String replaceTypesWithShortForms(String s){
    if(s==null)return null;
    return s
      .replace("{F}","[F]")
      .replace("{L}","[L]")
      .replace("{R}","[R]")
      .replace("{G}","[G]")
      .replace("{W}","[W]")
      .replace("{P}","[P]")
      .replace("{C}","[C]")
      .replace("{D}","[D]")
      .replace("{M}","[M]")
      .replace("{Y}","[Y]")
      .replace("{N}","[N]")
      .replace("Fighting Energy", "[F] Energy")
      .replace("Lightning Energy", "[L] Energy")
      .replace("Fire Energy", "[R] Energy")
      .replace("Grass Energy", "[G] Energy")
      .replace("Water Energy", "[W] Energy")
      .replace("Psychic Energy", "[P] Energy")
      .replace("Colorless Energy", "[C] Energy")
      .replace("Darkness Energy", "[D] Energy")
      .replace("Metal Energy", "[M] Energy")
      .replace("Fairy Energy", "[Y] Energy")
      .replace("Dragon Energy", "[N] Energy")
      .replace("Fighting Pokémon", "[F] Pokémon")
      .replace("Lightning Pokémon", "[L] Pokémon")
      .replace("Fire Pokémon", "[R] Pokémon")
      .replace("Grass Pokémon", "[G] Pokémon")
      .replace("Water Pokémon", "[W] Pokémon")
      .replace("Psychic Pokémon", "[P] Pokémon")
      .replace("Colorless Pokémon", "[C] Pokémon")
      .replace("Darkness Pokémon", "[D] Pokémon")
      .replace("Metal Pokémon", "[M] Pokémon")
      .replace("Fairy Pokémon", "[Y] Pokémon")
      .replace("Dragon Pokémon", "[N] Pokémon")
      .replace("Colorless", "[C]")
      .replace("Pokemon","Pokémon")
      .replace("`","'")
      .replace("–","-")
      ;
  }

  public static <T> Optional<T> parseNestedObject(JsonParser baseParser, JsonNode baseNode, String key, TypeReference<?> typeReference) throws IOException {
    JsonParser parser = getNestedObjectParser(baseParser, baseNode, key);
    if (parser == null) {
      return Optional.empty();
    }
    return Optional.of(parser.readValueAs(typeReference));
  }

  public static <T> Optional<T> parseNestedObject(JsonParser baseParser, JsonNode baseNode, String key, Class<T> classReference) throws IOException {
    JsonParser parser = getNestedObjectParser(baseParser, baseNode, key);
    if (parser == null) {
      return Optional.empty();
    }
    return Optional.of(parser.readValueAs(classReference));
  }

  private static JsonParser getNestedObjectParser(JsonParser baseParser, JsonNode baseNode, String key) {
    if (!baseNode.has(key)) return null;
    JsonParser newParser = baseNode.findValue(key).traverse();
    newParser.setCodec(baseParser.getCodec());
    return newParser;
  }
}
