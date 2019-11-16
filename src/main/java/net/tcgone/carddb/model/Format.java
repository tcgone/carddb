package net.tcgone.carddb.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;

import java.util.List;

public class Format {
  @NotBlank
  public String name;
  @NotBlank
  public String seoName;
  @NotBlank
  public String enumId;
  @NotBlank
  public String description;
  public String imageUrl;
  @Size(min = 1)
  public List<String> sets;
  public List<String> includes;
  public List<String> excludes;
  @NotBlank
  public String ruleSet;
  public int order;
  public List<String> flags;

  /**
   * sets incl. partials, populated at runtime
   */
  @JsonIgnore
  public List<Set> _sets;
  /**
   * all cards of this format, populated at runtime
   */
  @JsonIgnore
  public List<Card> _cards;
  private java.util.Set<Card> _cards_set;

  /**
   * Checks whether the other format is fully covered by this format.
   *
   * @param other format
   * @return true if all cards in other is already in this, false otherwise
   */
  public boolean fullyCovers(Format other) {
    return _cards.containsAll(other._cards);
  }

  public boolean hasCard(Card card) {
    if (_cards_set == null) {
      _cards_set = ImmutableSet.copyOf(_cards);
    }
    return _cards_set.contains(card);
  }
}
