package tcgone.carddb.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

public class Ability {
  public String type;
  public String name;
  public String text;

  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Ability ability = (Ability) o;
    return Objects.equals(type, ability.type) &&
      Objects.equals(name, ability.name) &&
      Objects.equals(text, ability.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, name, text);
  }
}
