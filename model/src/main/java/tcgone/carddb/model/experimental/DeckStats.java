package tcgone.carddb.model.experimental;

import lombok.Data;

/**
 * @author axpendix@hotmail.com
 * @since 20.05.2019
 */
@Data
public class DeckStats {
  private String id; // mongo id of the deck
  private java.util.Set<String> upVoteIds;
  private java.util.Set<String> downVoteIds;
  private int voteScore;
  private int timesWon; //?
  private int timesLost; //?
  private double rating; //?
  private int timesUsed;
}
