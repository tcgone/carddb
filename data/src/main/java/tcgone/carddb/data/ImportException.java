package tcgone.carddb.data;

import java.util.List;

public class ImportException extends Exception {
  public ImportException(String message) {
    super(message);
  }
  public ImportException(List<String> messages) {
    super(String.join("\n", messages));
  }
}
