package net.tcgone.carddb.model;

import javax.validation.constraints.NotBlank;

public class Variant {
    // the id of the variant, usually the first print of the card
    @NotBlank
    public String id;
    // Either one of: Regular (default if null), Reprint, Full Art, Secret Art, Promo, Other Half, Similar, Holo
    @NotBlank
    public String type;
}
