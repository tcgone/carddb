package net.tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.text.WordUtils;

/**
 * @author axpendix@hotmail.com
 */
public enum Rarity {
	SHINING,
	SECRET,
	PROMO,
	ULTRA_RARE,
	RARE_HOLO,
	RARE,
	UNCOMMON,
	COMMON;

	private final String label;

	Rarity() {
		label = WordUtils.capitalizeFully(name().replace('_', ' '));
	}

	@JsonCreator
	public static Rarity of(String label) {
		for (Rarity value : values()) {
			if (value.label.equalsIgnoreCase(label)) return value;
		}
//		return null;
		throw new IllegalArgumentException("Rarity for '"+label+"' was not found.");
	}

	@JsonValue
	@Override
	public String toString() {
		return label;
	}
}
