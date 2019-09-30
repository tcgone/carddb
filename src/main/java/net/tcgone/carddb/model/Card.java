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
package net.tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.tcgone.carddb.model.experimental.Variant;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Card {
	/**
	 * Experimental id: 101-4
	 */
	@NotBlank
	public String id;
	/**
	 * Pio id: base1-4
	 */
	@NotBlank
	public String pioId;
	/**
	 * Our id: CHARIZARD_4:BASE_SET
	 */
	@NotBlank
	public String enumId;
	/**
	 *
	 */
	public Set set;
	/**
	 * Card name: Charizard
	 */
	@NotBlank
	public String name;
	/**
	 * 4
	 */
	public Integer nationalPokedexNumber;
	/**
	 * 4
	 */
	@NotBlank
	public String number;
	/**
	 * Img url: https://tcgone.net/scans/m/base_set/004.jpg
	 */
	public String imageUrl;
	/**
	 * Img url: https://tcgone.net/scans/l/base_set/004.jpg
	 */
	public String imageUrlHiRes;
	/**
	 * Array of types: ["R"]
	 */
	public List<String> types;
	/**
	 * Either Pokémon, Trainer or Energy
	 */
	@NotBlank
	public String superType;
	/**
	 * Stage 2
	 */
	public List<String> subTypes;
	/**
	 * Has additional types like OWNERS_POKEMON, TEAM_PLASMA, etc. []
	 */
	public List<String> cardFlags;
	/**
	 * Charmeleon
	 */
	public String evolvesFrom;
	/**
	 *
	 */
	public List<String> evolvesTo;
	/**
	 * 120
	 */
	public Integer hp;
	/**
	 * 3
	 */
	public Integer retreatCost;
	/**
	 * [ {
	 "type": "Pokémon Power",
	 "name": "Energy Burn",
	 "text": "As often as you like during your turn (before your attack), you may turn all Energy attached to Charizard into [R] for the rest of the turn. This power can't be used if Charizard is Asleep, Confused, or Paralyzed."
	 } ]
	 */
	@Valid
	public List<Ability> abilities;
	/**
	 * [ {
	 "cost": ["R","R","R","R"],
	 "name": "Fire Spin",
	 "text": "Discard 2 Energy cards attached to Charizard in order to use this attack.",
	 "damage": "100",
	 "convertedEnergyCost": 4
	 } ]
	 */
	@Valid
	public List<Move> moves;
	/**
	 * [ {
	 "type": "W",
	 "value": "×2"
	 } ]
	 */
	@Valid
	public List<WeaknessResistance> weaknesses;
	/**
	 * [ {
	 "type": "F",
	 "value": "-30"
	 } ]
	 */
	@Valid
	public List<WeaknessResistance> resistances;
	/**
	 * Charizard (BS 4)
	 */
	public String fullName;
	/**
	 * 
	 */
	public String seoName;
	/**
	 * Rare Holo
	 */
	@NotBlank
	public String rarity;
	/**
	 * Epic
	 */
	public String careerClass;
	/**
	 * Trainer/Energy text/Pokemon ruling text. Each entry is a line.
	 */
	public List<String> text;
	/**
	 * Energy types
	 */
	public List<List<String>> energy;
	/**
	 * Mitsuhiro Arita
	 */
	public String artist;
	/**
	 * Spits fire that is hot enough to melt boulders. Known to unintentionally cause forest fires.
	 */
	public String flavorText;
	/**
	 * (null)
	 */
	public String implNotes;
	/**
	 * {@code pokemonPower {
			def set = [] as Set
			def eff1, eff2
			onActivate {
				if(eff1) eff1.unregister()
				if(eff2) eff2.unregister()
				eff1 = delayed {
					before BETWEEN_TURNS, {
						set.clear()
					}
				}
				eff2 = getter GET_ENERGY_TYPES, { holder->
					if(set.contains(holder.effect.card)) {
						int count = holder.object.size()
						holder.object = [(1..count).collect{[FIRE] as Set}]
					}
				}
			}
			actionA {
				assert !(self.specialConditions) : "$self is affected by a special condition"
				def newSet = [] as Set
				newSet.addAll(self.cards.filterByType(ENERGY))
				if(newSet != set){
					powerUsed()
					set.clear()
					set.addAll(newSet)
				} else {
					wcu "Nothing to burn more"
				}
			}
		}
		move {
			onAttack {
				damage 100
				discardSelfEnergy(C) // one energy card
				discardSelfEnergy(C) // one energy card
			}
		}}
	 */
	public String script;

	// Id of the copied card. Example: cardX
	public String copyOf;
	// How did this card get copied. Example, if THIS CARD is Holo version of cardX, put Holo here.
	public String copyType;

	// List of variants, managed by api unless explicitly specified.
	public List<Variant> variants;

	/**
	 * true when this has been merged with pio, so the definition is finalized.
	 * merged cards won't be attempted to be merged again, so the process can be
	 * restarted when failed.
	 */
	public Boolean merged;
	/**
	 * Sort order (respective to its set)
	 */
	public Integer order;
	
	/**
	 * Legal format seoNames (generated at runtime)
	 */
	public List<String> formats;

	/**
	 * Contains derived full plain format text
	 */
	public String fullText;

	/**
	 * Contains derived full seo title
	 */
	public String seoTitle;

	// public String stage;

	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Card card = (Card) o;
		return Objects.equals(id, card.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
