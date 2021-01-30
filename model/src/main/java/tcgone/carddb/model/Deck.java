package tcgone.carddb.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author axpendix@hotmail.com
 * @since 20.05.2019
 */
@Data
public class Deck {
  private String id; // external id, generated randomly
  private String legacyId; // old internal mongo id, stored for legacy decks
  private String seoName; // generated seo name except id
  @NotBlank
  @Size(max = 50)
  private String name;
  @NotBlank
  private String format; // main format
  @Size(max = 4096)
  private String description;
  private String creatorId;
  @NotNull
  private Map<String, Integer> contents; // contents with new id
  private List<Tag> tags = new ArrayList<>(); // theme, public, private, draft, career, list
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
}
