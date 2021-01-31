package tcgone.carddb.data;

public class ConstraintViolation {
  private String context;
  private String message;

  public ConstraintViolation(String context, String message) {
    this.context = context;
    this.message = message;
  }

  public String getContext() {
    return context;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "ConstraintViolation{" +
      "context='" + context + '\'' +
      ", message='" + message + '\'' +
      '}';
  }
}
