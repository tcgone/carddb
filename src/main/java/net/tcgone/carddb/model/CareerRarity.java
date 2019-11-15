package net.tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.text.WordUtils;

/**
 * @author axpendix@hotmail.com
 */
public enum CareerRarity {
	EPIC,
	SUPERIOR,
	RARE,
	AVERAGE,
	FREQUENT,
	BASIC,
	HOLO,
	NOT_AVAILABLE;

	private final String label;

	CareerRarity() {
		label = WordUtils.capitalizeFully(name().replace('_', ' '));
	}

	@JsonCreator
	public static CareerRarity of(String label) {
		for (CareerRarity value : values()) {
			if (value.label.equalsIgnoreCase(label)) return value;
		}
//		return null;
		throw new IllegalArgumentException("CareerRarity for '"+label+"' was not found.");
	}

	@JsonValue
	@Override
	public String toString() {
		return label;
	}
}
