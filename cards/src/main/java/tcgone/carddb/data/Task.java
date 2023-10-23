package tcgone.carddb.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tcgone.carddb.model.Card;
import tcgone.carddb.model.EnhancedCard;

@Data
@EqualsAndHashCode(of = {"base", "variant", "field"})
public class Task {
  private final EnhancedCard base;
  private final EnhancedCard variant;
  private final MergeField field;
  private Card result;
  private boolean done;
  private boolean skipped;
}
