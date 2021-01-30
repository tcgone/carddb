/*
Copyright 2018 axpendix@hotmail.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package tcgone.carddb.tools;

import org.apache.commons.lang3.text.WordUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * @author axpendix@hotmail.com
 */
public enum CoreCollection {

	//CLASSIC
	BASE_SET(111, "BS", null),
	JUNGLE(112, "JU", null),
	FOSSIL(113, "FO", null),
	BASE_SET_2(114, "BS2", null),
	TEAM_ROCKET(115, "TR", null),

	//GYM
	GYM_HEROES(121, "G1", null),
	GYM_CHALLENGE(122, "G2", null),

	WIZARDS_BLACK_STAR_PROMOS(130, "WBSP", null),
	VENDING_MACHINE(131, "VM", null),
	SOUTHERN_ISLANDS(132, "SI", null),
	LEGENDARY_COLLECTION(133, "LC", null),

	//NEO
	NEO_GENESIS(161, "N1", null),
	NEO_DISCOVERY(162, "N2", null),
	NEO_REVELATION(163, "N3", null),
	NEO_DESTINY(164, "N4", null),

	//E-CARD
	EXPEDITION(171, "EXP", "ecard1"),
	AQUAPOLIS(172, "AQP", "ecard2"),
	SKYRIDGE(173, "SKR", "ecard3"),

	//EX
	RUBY_SAPPHIRE(211, "Ruby & Sapphire", "RS", null),
	SANDSTORM(212, "SS", null),
	DRAGON(213, "DR", null),
	TEAM_MAGMA_VS_TEAM_AQUA(214, "Team Magma vs Team Aqua", "MA", null),
	HIDDEN_LEGENDS(215, "HL", null),
	FIRERED_LEAFGREEN(216, "FireRed & LeafGreen", "FRLG", null),
	TEAM_ROCKET_RETURNS(217, "TRR", null),
	DEOXYS(218, "DX", null),
	EMERALD(219, "EM", null),
	UNSEEN_FORCES(220, "UF", null),
	DELTA_SPECIES(221, "DS", null),
	LEGEND_MAKER(222, "LM", null),
	HOLON_PHANTOMS(223, "HP", null),
	CRYSTAL_GUARDIANS(224, "CG", null),
	DRAGON_FRONTIERS(225, "DF", null),
	POWER_KEEPERS(226, "PK", null),

	//DIAMOND & PEARL
	DIAMOND_PEARL(251, "Diamond & Pearl", "DP", null),
	MYSTERIOUS_TREASURES(252, "MT", null),
	SECRET_WONDERS(253, "SW", null),
	GREAT_ENCOUNTERS(254, "GE", null),
	MAJESTIC_DAWN(255, "MD", null),
	LEGENDS_AWAKENED(256, "LA", null),
	STORMFRONT(257, "SF", null),

	//PLATINUM
	PLATINUM(261, "PL", null),
	RISING_RIVALS(262, "RR", null),
	SUPREME_VICTORS(263, "SV", null),
	ARCEUS(264, "AR", null),

	//HEARTGOLD & SOULSILVER
	HEARTGOLD_SOULSILVER(271, "HeartGold & SoulSilver", "HGSS", null),
	UNLEASHED(272, "UL", null),
	UNDAUNTED(273, "UD", null),
	TRIUMPHANT(274, "TM", null),
	CALL_OF_LEGENDS(275, "Call of Legends", "CL", null),

	POP_SERIES_1(281, "POP Series 1", "POP1", null),
	POP_SERIES_2(282, "POP Series 2", "POP2", null),
	POP_SERIES_3(283, "POP Series 3", "POP3", null),
	POP_SERIES_4(284, "POP Series 4", "POP4", null),
	POP_SERIES_5(285, "POP Series 5", "POP5", null),
	POP_SERIES_6(286, "POP Series 6", "POP6", null),
	POP_SERIES_7(287, "POP Series 7", "POP7", null),
	POP_SERIES_8(288, "POP Series 8", "POP8", null),
	POP_SERIES_9(289, "POP Series 9", "POP9", null),

	//BLACK & WHITE
	BLACK_WHITE_PROMOS(310, "Black & White Promos", "BLWP", null),
	BLACK_WHITE(311, "Black & White", "BLW", null),
	EMERGING_POWERS(312, "EPO", null),
	NOBLE_VICTORIES(313, "NVI", null),
	NEXT_DESTINIES(314, "NXD", null),
	DARK_EXPLORERS(315, "DEX", null),
	DRAGONS_EXALTED(316, "DRX", null),
	DRAGON_VAULT(317, "DRV", null),
	BOUNDARIES_CROSSED(318, "BCR", null),
	PLASMA_STORM(319, "PLS", null),
	PLASMA_FREEZE(320, "PLF", null),
	PLASMA_BLAST(321, "PLB", null),
	LEGENDARY_TREASURES(322, "LTR", null),

	//XY
	KALOS_STARTER_SET(359, "KSS", null),
  XY_PROMOS(360, "XY Promos", "XYP", null),
	XY(361, "XY", "XY", null),
	FLASHFIRE(362, "FLF", null),
	FURIOUS_FISTS(363, "FUF", null),
	PHANTOM_FORCES(364, "PHF", null),
	PRIMAL_CLASH(365, "PCL", null),
	DOUBLE_CRISIS(366, "DCR", null),
	ROARING_SKIES(367, "ROS", null),
	ANCIENT_ORIGINS(368, "AOR", null),
	BREAKTHROUGH(369, "BREAKthrough", "BKT", null),
	BREAKPOINT(370, "BREAKpoint", "BKP", null),
	GENERATIONS(371, "GEN", null),
	FATES_COLLIDE(372, "FCO", null),
	STEAM_SIEGE(373, "STS", null),
	EVOLUTIONS(374, "EVO", null),

	//Sun & Moon
	SUN_MOON_PROMOS(410, "Sun & Moon Promos", "SMP", "smp"),
	SUN_MOON(411, "Sun & Moon", "SM", null),
	GUARDIANS_RISING(412, "GRI", null),
	BURNING_SHADOWS(413, "BUS", null),
	SHINING_LEGENDS(414, "SLG", null),
	CRIMSON_INVASION(415, "CIN", null),
	ULTRA_PRISM(416, "UPR", null),
	FORBIDDEN_LIGHT(417, "FLI", null),
	CELESTIAL_STORM(418, "CLS", null),
	DRAGON_MAJESTY(419, "DRM", null),
	LOST_THUNDER(420, "LOT", "sm8"),
	TEAM_UP(421, "TMU", "sm9"),
	DETECTIVE_PIKACHU(422, "DET", "det1"),
	UNBROKEN_BONDS(423, "UNB", "sm10"),
	UNIFIED_MINDS(424, "UNM", "sm11"),
	HIDDEN_FATES(425, "HIF", "hif"),
	SHINY_VAULT(426, "SMA", "sma"),
	COSMIC_ECLIPSE(427, "CEC", "sm12"),

	// Sword & Shield
	SWORD_SHIELD(430, "SSH", "swsh1"),
	REBEL_CLASH(431, "RCL", "swsh2"),

	//POKEMOD
	POKEMOD_BASE_SET(911, "PMDBS", null),
	POKEMOD_JUNGLE(912, "PMDJU", null),
	POKEMOD_FOSSIL(913, "PMDFO", null),
	POKEMOD_TEAM_ROCKET(914, "PMDTR", null),
	POKEMOD_GYM_HEROES(915, "PMDG1", null),
	POKEMOD_GYM_CHALLENGE(916, "PMDG2", null),
	POKEMOD_NEO_GENESIS(917, "PMDN1", null),
	POKEMOD_NEO_DISCOVERY(918, "PMDN2", null),
	POKEMOD_NEO_REVELATION(919, "PMDN3", null),
	POKEMOD_NEO_DESTINY(920, "PMDN4", null),
	POKEMOD_EXPEDITION(921, "PMDEXP", null),
	POKEMOD_AQUAPOLIS(922, "PMDAQP", null),
	POKEMOD_SKYRIDGE(923, "PMDSKR", null),
	POKEMOD_VENDING_MACHINE(924, "PMDVM", null),
	POKEMOD_PROMOS(925, "PMDPRO", null),
//	POKEMOD_(900, "PMD"),

	;

	private int id;
	private String shortName;
	private String fullName;
	private String pioCode;

	CoreCollection(int id, String shortName, String pioCode) {
		this(id, null, shortName, pioCode);
	}

	CoreCollection(int id, String fullName, String shortName, String pioCode) {
		this.id = id;
		this.fullName = fullName;
		this.shortName = shortName;
		this.pioCode = pioCode;
	}

	public String getShortName(){
		return shortName;
	}

	public String getName(){
		return fullName==null? WordUtils.capitalizeFully(this.name(), "_".toCharArray()).replaceAll("_", " "):fullName;
	}

	public int getId() {
		return id;
	}

	public String getPioCode() {
		return pioCode;
	}

	public static Optional<CoreCollection> findById(int id){
		for (CoreCollection item : values()) {
			if(item.id == id) return Optional.of(item);
		}
		return Optional.empty();
	}

	public static Optional<CoreCollection> findByName(String name){
		for(CoreCollection item : values()){
			if(Objects.equals(item.getName(), name)) return Optional.of(item);
		}
		return Optional.empty();
	}

	public static Optional<CoreCollection> findByShortName(String shortName){
		for(CoreCollection item : values()){
			if(Objects.equals(item.shortName, shortName)) return Optional.of(item);
		}
		return Optional.empty();
	}

	public static Optional<CoreCollection> findByPioCode(String pioCode){
		for(CoreCollection item : values()){
			if(item.pioCode != null && Objects.equals(item.pioCode, pioCode)) return Optional.of(item);
		}
		return Optional.empty();
	}

}
