package tcgone.carddb.data;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ImportException extends Exception {
  private final List<ConstraintViolation> violations;

  public ImportException(String message) {
    super(message);
    this.violations = new ArrayList<>();
  }
  public ImportException(String message, List<ConstraintViolation> violations) {
    super(String.format("%s;\n\nValidation Errors:\n%s", message, violations.stream().map(ConstraintViolation::toString).collect(Collectors.joining("\n"))));
    this.violations = violations;
  }
}
