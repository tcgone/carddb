package net.tcgone.carddb.model;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author axpendix@hotmail.com
 * @since 20.05.2019
 */
@Data
public class Deck {
    private String id; // external id
    private String oldId; // old internal mongo id, stored for legacy decks
    private String seoName; // generated seo name except id
    private String name;
    private String format; // main format
    private String description;
    private String creatorId;
    private String creatorUsername; // not serialized
    private Map<String, Integer> contentsEnumId; // contents with enumId (legacy)
    private Map<String, Integer> contents; // contents with new id
    private Date lastUpdated;
    private List<String> tags; // theme, public, private, draft, career, list
    private List<String> colors; // fire, grass, psychic
    private List<String> tiers; // tournament, tier1, tier2, tier3, experimental, other, fun
    private List<String> variants; // unholy paladin, haymaker
    private List<String> formats; // all valid formats that this can be played in
}
