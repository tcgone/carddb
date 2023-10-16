package tcgone.carddb.data;

import lombok.Getter;

@Getter
public class ConstraintViolation {
  private final String context;
  private final String message;

  public ConstraintViolation(String context, String message) {
    this.context = context;
    this.message = message;
  }

  @Override
  public String toString() {
    return "Violation{" +
      "context='" + context + '\'' +
      ", message='" + message + '\'' +
      '}';
  }
}
