package net.tcgone.carddb.model;

import org.apache.commons.text.WordUtils;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Experimental
 *
 * @author axpendix@hotmail.com
 */
public enum CardType {

	POKEMON(10),
	ENERGY(20),
	TRAINER(30),

	MEGA_POKEMON(-6),
	LEVEL_UP(-5),
	BREAK(-4),
	STAGE2(-2),
	STAGE1(-1),
	EVOLUTION(0),
	RESTORED(4),
	BASIC(7),
	BABY(8),
	LEGEND(9),

	BASIC_ENERGY(18),
	SPECIAL_ENERGY(19),
	ITEM(23),
	SUPPORTER(24),
	STADIUM(25),
	POKEMON_TOOL(26),
	TECHNICAL_MACHINE(27),
	FLARE(28),
	ROCKETS_SECRET_MACHINE(29),

	TAG_TEAM(93),
	ULTRA_BEAST(94),
	PRISM_STAR(95),
	POKEMON_GX(96, "Pokémon-GX"),
	POKEMON_PRIME(97),
	POKEMON_STAR(98),
	POKEMON_EX(99, "Pokémon-EX"), //UPPERCASE
	EX(100, "Pokémon-ex"), //LOWERCASE
	FOSSIL(101),
	TEAM_MAGMA(102),
	TEAM_AQUA(103),
	OWNERS_POKEMON(104),
	DARK_POKEMON(105),
	LIGHT_POKEMON(106),
	SHINING_POKEMON(107),
	TEAM_PLASMA(108),
	ACE_SPEC(109),
	HAS_ANCIENT_TRAIT(110),

	NOT_IMPLEMENTED(201),

	;

	private final int priority;
	private final String label;

	CardType(int priority) {
		this(priority, null);
	}

	CardType(int priority, String label) {
		this.priority = priority;
		if (label != null) {
			this.label = label;
		} else {
			this.label = WordUtils.capitalizeFully(name().replace("_", " ")).replace("Pokemon", "Pokémon");
		}
	}

	public int getPriority() {
		return priority;
	}

	public String getLabel() {
		return label;
	}

	public static class CardTypeComparator implements Comparator<CardType>, Serializable {
		@Override
		public int compare(CardType o1, CardType o2) {
			return Integer.compare(o1.priority, o2.priority);
		}
	}

	public boolean isSuperType() {
		return this == POKEMON || this == TRAINER || this == ENERGY;
	}

}
