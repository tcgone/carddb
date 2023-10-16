package tcgone.carddb.model;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Models a card (variant) within a group of similar cards (variance).
 */
@Data
public class Variant {
  /**
   * Id of this variant
   */
  private String id;
  /**
   * Type of this variant
   */
  private VariantType type;
  /**
   * Shows the true equivalence/copy id.
   * Only cards with the same copyId are truly equivalent in engine's terms.
   */
  private String copyId;

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
      .append("id", id)
      .append("type", type)
      .toString();
  }

}
