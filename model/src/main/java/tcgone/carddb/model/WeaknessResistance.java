package tcgone.carddb.model;

import lombok.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

/**
 * @author axpendix@hotmail.com
 * @since 08.10.2018
 */
@Data
public class WeaknessResistance {
  private Type type;
  private String value;

  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
  }
}
