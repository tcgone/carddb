package tcgone.carddb.merger;

import lombok.Data;
import tcgone.carddb.data.MergeField;

@Data
class SkippedTask {
  String base;
  String variant;
  MergeField field;
}
