package tcgone.carddb.model3;

import lombok.Value;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Represents an expansion definition file on disk
 */
@Value
public class ExpansionFile3 {
  /**
   * Schema and version
   */
  String schema;
  /**
   * Expansion
   */
  Expansion3 expansion;
  /**
   * Cards in expansion
   */
  List<Card3> cards;

  public String generateFileName() {
    return String.format("%s-%s", expansion.getOrderId(), expansion.getEnumId().toLowerCase(Locale.ENGLISH));
  }

  public String generateFinalFilePath(String outputDirectory) {
    return String.format("%s%s%s.yaml", outputDirectory, File.separatorChar, generateFileName());
  }
}
