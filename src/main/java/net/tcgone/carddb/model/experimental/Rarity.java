package net.tcgone.carddb.model.experimental;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.text.WordUtils;

public enum Rarity {
	SHINING,
	SECRET,
	PROMO,
	ULTRA_RARE,
	RARE_HOLO,
	RARE,
	UNCOMMON,
	COMMON;

	private String label;

	Rarity(){
		this.label = WordUtils.capitalizeFully(name().replace("_"," "));
	}

	@JsonCreator
	public static Rarity of(String label){
		for (Rarity rarity : values()) {
			if(rarity.label.equalsIgnoreCase(label)) return rarity;
		}
		return null;
//		throw new IllegalArgumentException("Rarity for '"+label+"' was not found.");
	}

	@JsonValue
	public String getLabel() {
		return label;
	}
}
