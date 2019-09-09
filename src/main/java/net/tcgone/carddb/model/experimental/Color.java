package net.tcgone.carddb.model.experimental;

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
public enum Color {

	COLORLESS("C"),
	FIRE("R"),
	GRASS("G"),
	WATER("W"),
	LIGHTNING("L"),
	FIGHTING("F"),
	PSYCHIC("P"),
	DARKNESS("D"),
	METAL("M"),
	FAIRY("Y"),

	RAINBOW(),
	MAGMA(),
	AQUA(),
	DRAGON();

	private String notation;

	Color(String notation) {
		this.notation = notation;
	}

	Color() {
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
	public static Color from(String notation){
		if(notation == null) return null;
 		notation = notation.toUpperCase(Locale.ENGLISH);
		for (Color type : values()) {
			if(notation.equals(type.notation) || notation.equals(type.name()))
				return type;
		}
		return null;
	}
	
	public static List<Color> valuesForBasicEnergy(){
		return Arrays.asList(COLORLESS, FIRE, GRASS, WATER, LIGHTNING, FIGHTING, PSYCHIC, DARKNESS, METAL, FAIRY);
	}

	public static List<Color> valuesForPokemon(){
		return Arrays.asList(COLORLESS, FIRE, GRASS, WATER, LIGHTNING, FIGHTING, PSYCHIC, DARKNESS, METAL, FAIRY, DRAGON);
	}

}
