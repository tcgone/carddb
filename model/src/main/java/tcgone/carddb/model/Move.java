package tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Move {
  public List<Type> cost;
  public String name;
  public String damage;
  public String text;

  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Move move = (Move) o;
    return Objects.equals(cost, move.cost) &&
      Objects.equals(name, move.name) &&
      Objects.equals(damage, move.damage) &&
      Objects.equals(text, move.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cost, name, damage, text);
  }
}
