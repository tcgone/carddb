package tcgone.carddb.data;

import lombok.Getter;

@Getter
public class ConstraintViolation {

  private final String context;
  private final String message;
  private Task task;

  public ConstraintViolation(String context, String message) {
    this.context = context;
    this.message = message;
  }

  public ConstraintViolation setTask(Task task) {
    this.task = task;
    return this;
  }

  @Override
  public String toString() {
    return "Violation{" +
      "context='" + context + '\'' +
      ", message='" + message + '\'' +
      '}';
  }
}
