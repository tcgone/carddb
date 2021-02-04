package tcgone.carddb.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Variant {
  /**
   * the id of auto-calculated variance record chain, usually the first print of the card
   */
  public String id;
  /**
   *
   */
  public VariantType type;

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
      .append("id", id)
      .append("type", type)
      .toString();
  }
}
