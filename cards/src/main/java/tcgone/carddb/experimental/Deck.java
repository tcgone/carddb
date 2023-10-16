package tcgone.carddb.experimental;

import tcgone.carddb.model.Type;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author axpendix@hotmail.com
 * @since 20.05.2019
 */
public class Deck {
  private String id; // external id, generated randomly
  private String legacyId; // old internal mongo id, stored for legacy decks
  private String seoName; // generated seo name except id
//  @NotBlank
//  @Size(max = 50)
  private String name;
//  @NotBlank
  private String format; // main format
//  @Size(max = 4096)
  private String description;
  private String creatorId;
//  @NotNull
  private Map<String, Integer> contents; // contents with new id
  private List<DeckTag> tags = new ArrayList<>(); // theme, public, private, draft, career, list
  private List<Type> types = new ArrayList<>(); // fire, grass, psychic
  private List<String> validFormats = new ArrayList<>(); // all valid formats that this can be played in
  //  private List<String> tiers; // tournament, tier1, tier2, tier3, experimental, other, fun
//  private List<String> variants; // unholy paladin, haymaker
  private Date lastUpdated;
  private int timesUsed;
  private boolean playable;
  private String errorMessage;

  public int size() {
    int count = 0;
    if (contents != null) {
      for (Integer value : contents.values()) {
        count += value;
      }
    }
    return count;
  }

  public String getId() {
    return id;
  }

  public Deck setId(String id) {
    this.id = id;
    return this;
  }

  public String getLegacyId() {
    return legacyId;
  }

  public Deck setLegacyId(String legacyId) {
    this.legacyId = legacyId;
    return this;
  }

  public String getSeoName() {
    return seoName;
  }

  public Deck setSeoName(String seoName) {
    this.seoName = seoName;
    return this;
  }

  public String getName() {
    return name;
  }

  public Deck setName(String name) {
    this.name = name;
    return this;
  }

  public String getFormat() {
    return format;
  }

  public Deck setFormat(String format) {
    this.format = format;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Deck setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public Deck setCreatorId(String creatorId) {
    this.creatorId = creatorId;
    return this;
  }

  public Map<String, Integer> getContents() {
    return contents;
  }

  public Deck setContents(Map<String, Integer> contents) {
    this.contents = contents;
    return this;
  }

  public List<DeckTag> getTags() {
    return tags;
  }

  public Deck setTags(List<DeckTag> tags) {
    this.tags = tags;
    return this;
  }

  public List<Type> getTypes() {
    return types;
  }

  public Deck setTypes(List<Type> types) {
    this.types = types;
    return this;
  }

  public List<String> getValidFormats() {
    return validFormats;
  }

  public Deck setValidFormats(List<String> validFormats) {
    this.validFormats = validFormats;
    return this;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public Deck setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
    return this;
  }

  public int getTimesUsed() {
    return timesUsed;
  }

  public Deck setTimesUsed(int timesUsed) {
    this.timesUsed = timesUsed;
    return this;
  }

  public boolean isPlayable() {
    return playable;
  }

  public Deck setPlayable(boolean playable) {
    this.playable = playable;
    return this;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public Deck setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }
}
