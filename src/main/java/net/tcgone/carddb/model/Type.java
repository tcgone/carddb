package net.tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Experimental
 *
 * @author axpendix@hotmail.com
 */
public enum Type {

	GRASS("G"),
	FIRE("R"),
	WATER("W"),
	LIGHTNING("L"),
	PSYCHIC("P"),
	FIGHTING("F"),
	DARKNESS("D"),
	METAL("M"),
	FAIRY("Y"),
	DRAGON("N"),
	COLORLESS("C"),

	RAINBOW,
	MAGMA,
	AQUA;

	private String notation;

	Type(String notation) {
		this.notation = notation;
	}

	Type() {
		this.notation = name();
	}

	@JsonValue
	public String getNotation() {
		return notation;
	}

	public String getEnclosedNotation(){
		return "[" + notation + "]";
	}

	@JsonCreator
	public static Type of(String notation){
		if(notation == null) return null;
 		notation = notation.toUpperCase(Locale.ENGLISH);
		for (Type type : values()) {
			if(notation.equals(type.notation) || notation.equals(type.name()))
				return type;
		}
		return null;
	}

	public static List<Type> valuesForPokemon(){
		return Arrays.asList(GRASS, FIRE, WATER, LIGHTNING, PSYCHIC, FIGHTING, DARKNESS, METAL, FAIRY, DRAGON, COLORLESS);
	}

}
