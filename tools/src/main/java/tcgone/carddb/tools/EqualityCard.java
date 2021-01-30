package tcgone.carddb.tools;

import tcgone.carddb.model.Card;

import java.util.Objects;

/**
 * @author axpendix@hotmail.com
 * @since 06.06.2019
 */
public class EqualityCard extends Card {

	public EqualityCard(Card card){
		this.name=card.name;
		this.types=card.types;
		this.superType=card.superType;
		this.subTypes=card.subTypes;
		this.evolvesFrom=card.evolvesFrom;
		this.hp=card.hp;
		this.retreatCost=card.retreatCost;
		this.abilities=card.abilities;
		this.moves=card.moves;
		this.weaknesses=card.weaknesses;
		this.resistances=card.resistances;
		this.text=card.text;
		this.energy=card.energy;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EqualityCard card = (EqualityCard) o;
		return Objects.equals(name, card.name) &&
				Objects.equals(types, card.types) &&
				Objects.equals(superType, card.superType) &&
				Objects.equals(subTypes, card.subTypes) &&
				Objects.equals(evolvesFrom, card.evolvesFrom) &&
				Objects.equals(hp, card.hp) &&
				Objects.equals(retreatCost, card.retreatCost) &&
				Objects.equals(abilities, card.abilities) &&
				Objects.equals(moves, card.moves) &&
				Objects.equals(weaknesses, card.weaknesses) &&
				Objects.equals(resistances, card.resistances) &&
				Objects.equals(text, card.text) &&
				Objects.equals(energy, card.energy);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, types, superType, subTypes, evolvesFrom, hp, retreatCost, abilities, moves, weaknesses, resistances, text, energy);
	}
}
