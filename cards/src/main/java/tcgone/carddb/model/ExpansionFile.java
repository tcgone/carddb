package tcgone.carddb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Represents an expansion definition file on disk
 */
@Value
@Builder @Jacksonized
@AllArgsConstructor
public class ExpansionFile {
  /**
   * Schema and version
   */
  String schema;
  /**
   * Expansion
   */
  Expansion expansion;
  /**
   * Cards in expansion
   */
  List<Card> cards;

  public String generateFileName() {
    return String.format("%s-%s", expansion.getOrderId(), expansion.getEnumId().toLowerCase(Locale.ENGLISH));
  }

  public String generateFinalFilePath(String outputDirectory) {
    return String.format("%s%s%s.yaml", outputDirectory, File.separatorChar, generateFileName());
  }
}
