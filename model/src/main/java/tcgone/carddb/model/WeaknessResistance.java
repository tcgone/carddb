package tcgone.carddb.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

/**
 * @author axpendix@hotmail.com
 * @since 08.10.2018
 */
public class WeaknessResistance {
  public Type type;
  public String value;

  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WeaknessResistance that = (WeaknessResistance) o;
    return Objects.equals(type, that.type) &&
      Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, value);
  }
}
