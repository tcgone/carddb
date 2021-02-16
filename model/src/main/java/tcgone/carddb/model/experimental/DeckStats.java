package tcgone.carddb.model.experimental;


import java.util.Set;

/**
 * @author axpendix@hotmail.com
 * @since 20.05.2019
 */
public class DeckStats {
  private String id; // mongo id of the deck
  private java.util.Set<String> upVoteIds;
  private java.util.Set<String> downVoteIds;
  private int voteScore;
  private int timesWon; //?
  private int timesLost; //?
  private double rating; //?
  private int timesUsed;

  public String getId() {
    return id;
  }

  public DeckStats setId(String id) {
    this.id = id;
    return this;
  }

  public Set<String> getUpVoteIds() {
    return upVoteIds;
  }

  public DeckStats setUpVoteIds(Set<String> upVoteIds) {
    this.upVoteIds = upVoteIds;
    return this;
  }

  public Set<String> getDownVoteIds() {
    return downVoteIds;
  }

  public DeckStats setDownVoteIds(Set<String> downVoteIds) {
    this.downVoteIds = downVoteIds;
    return this;
  }

  public int getVoteScore() {
    return voteScore;
  }

  public DeckStats setVoteScore(int voteScore) {
    this.voteScore = voteScore;
    return this;
  }

  public int getTimesWon() {
    return timesWon;
  }

  public DeckStats setTimesWon(int timesWon) {
    this.timesWon = timesWon;
    return this;
  }

  public int getTimesLost() {
    return timesLost;
  }

  public DeckStats setTimesLost(int timesLost) {
    this.timesLost = timesLost;
    return this;
  }

  public double getRating() {
    return rating;
  }

  public DeckStats setRating(double rating) {
    this.rating = rating;
    return this;
  }

  public int getTimesUsed() {
    return timesUsed;
  }

  public DeckStats setTimesUsed(int timesUsed) {
    this.timesUsed = timesUsed;
    return this;
  }
}
