package net.tcgone.carddb.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    
    @JsonIgnore
    public List<Set> _sets;
    @JsonIgnore
    public java.util.Set<Card> _cards;

    public boolean fullyCovers(Format format){
        // TODO
        return false;
    }
    public boolean hasCard(Card card){
        // TODO
        return false;
    }
}
