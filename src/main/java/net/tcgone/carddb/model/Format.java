package net.tcgone.carddb.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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
    public boolean enabled;
    @NotBlank
    public String ruleSet;
    public int order;
}
