package tcgone.carddb.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Models a a card (variant) within a group of similar cards (variance).
 */
public class Variant {
  /**
   * Id of this variant
   */
  public String id;
  /**
   * Type of this variant
   */
  public VariantType type;
  /**
   * Shows the true equivalence/copy id.
   * Only cards with the same copyId are truly equivalent in engine's terms.
   */
  public String copyId;

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
      .append("id", id)
      .append("type", type)
      .toString();
  }
}
