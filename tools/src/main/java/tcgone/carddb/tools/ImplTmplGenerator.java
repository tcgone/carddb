package tcgone.carddb.tools;

import tcgone.carddb.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.stereotype.Component;
import tcgone.carddb.model.Set;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;

/**
 * @author axpendix@hotmail.com
 */
@Component
public class ImplTmplGenerator {

	public void writeAll(Collection<Set> setFiles) throws Exception {
		new File("impl").mkdirs();
		for (Set setFile : setFiles) {
			write(setFile);
		}
	}
	private void write(Set setFile) throws Exception {
		/*
		 * set.vm requires:
		 * classname foldername collection
		 * list1: name (enum const name), fullname, cardtype (enum const)
		 * rarity (enum const), cln (collection line no)
		 * list2: name (enum const name), impl (new x())
		 */
		Map<String, Object> modelmap = new HashMap<>();
		List<List1Item> list1 = new ArrayList<>();
		List<List2Item> list2 = new ArrayList<>();
		String EXPNNAME=setFile.enumId;
		modelmap.put("classname", WordUtils.capitalizeFully(EXPNNAME.replaceAll("_"," ")).replaceAll(" ",""));
		modelmap.put("foldername", EXPNNAME.toLowerCase(Locale.ENGLISH));
		modelmap.put("collection", EXPNNAME);
		modelmap.put("list1", list1);
		modelmap.put("list2", list2);

		// the magic happens here
		block_card:
		for(Card card:setFile.cards){
			List<String> cardTypeSet = new ArrayList<>();
			String rarity = null;
			String rc = String.valueOf(card.retreatCost);
			String hp = null;
			String predecessor = null;
			String cardtext = null;
			if(card.text!=null) {
				cardtext=StringUtils.join(card.text,"\" +\n\t\t\t\t\t\"");
			}
			String typesCombined = null;
			StringBuilder weakness = new StringBuilder();
			StringBuilder resistance = new StringBuilder();
			StringBuilder moves = new StringBuilder();
			StringBuilder abilities = new StringBuilder();

			// rarity
			if (card.rarity == Rarity.RARE_HOLO){
				rarity="HOLORARE";
			} else if (card.rarity == Rarity.ULTRA_RARE){
				rarity="ULTRARARE";
			} else {
			  rarity = card.rarity.name();
      }
			cardTypeSet.add(card.superType.name());
      for (CardType subType : card.subTypes) {
        cardTypeSet.add(subType.name());
      }
			if(card.superType == CardType.POKEMON) {
				//hp
				hp = String.valueOf(card.hp);
				if (hp.length() < 3) {
					hp = "0" + hp;
				}
				hp = String.format("HP%s", hp);
				//types
				if (card.types != null) {
					if (card.types.size() == 1) {
						typesCombined = card.types.get(0).getNotation();
					} else {
						typesCombined = card.types.toString();
					}
					for (Type _type : card.types) {
            cardTypeSet.add("_" + _type.name() + "_");
					}
				}
				if (card.subTypes.contains(CardType.BABY)) {
					cardTypeSet.add("BASIC");
				}
				predecessor=card.evolvesFrom;
				if(card.weaknesses!=null){
					for (WeaknessResistance wr : card.weaknesses) {
						weakness.append(String.format("weakness %s%s\n\t\t\t\t", wr.type, !wr.value.equalsIgnoreCase("x2")?", '"+wr.value+"'":""));
					}
				}
				if(card.resistances!=null){
					for (WeaknessResistance wr : card.resistances) {
						String typ = "";
						if("-20".equals(wr.value)) typ=", MINUS20";
						if("-30".equals(wr.value)) typ=", MINUS30";
						resistance.append(String.format("resistance %s\n\t\t\t\t", wr.type+typ));
					}
				}
				if(card.abilities!=null) {
					for (Ability a : card.abilities) {
						if(a.type.equalsIgnoreCase("Pokémon Power")) {
							abilities.append(String.format("pokemonPower \"%s\", {\n" +
									"\t\t\t\t\ttext \"%s\"\n" +
									"\t\t\t\t\tactionA {\n" +
									"\t\t\t\t\t}\n" +
									"\t\t\t\t}\n\t\t\t\t", a.name, a.text));
						}
						if(a.type.equalsIgnoreCase("Poké-Power")) {
							abilities.append(String.format("pokePower \"%s\", {\n" +
									"\t\t\t\t\ttext \"%s\"\n" +
									"\t\t\t\t\tactionA {\n" +
									"\t\t\t\t\t}\n" +
									"\t\t\t\t}\n\t\t\t\t", a.name, a.text));
						}
						if(a.type.equalsIgnoreCase("Poké-Body")) {
							abilities.append(String.format("pokeBody \"%s\", {\n" +
									"\t\t\t\t\ttext \"%s\"\n" +
									"\t\t\t\t\tdelayedA {\n" +
									"\t\t\t\t\t}\n" +
									"\t\t\t\t}\n\t\t\t\t", a.name, a.text));
						}
						if(a.type.equalsIgnoreCase("Ability")) {
							abilities.append(String.format("bwAbility \"%s\", {\n" +
									"\t\t\t\t\ttext \"%s\"\n" +
									"\t\t\t\t\tactionA {\n" +
									"\t\t\t\t\t}\n" +
									"\t\t\t\t}\n\t\t\t\t", a.name, a.text));
						}
						if(a.type.equalsIgnoreCase("Ancient Trait") || a.name.startsWith("Ω") || a.name.startsWith("α") || a.name.startsWith("Δ") || a.name.startsWith("θ")) {
							abilities.append(String.format("ancientTrait \"%s\", {\n" +
									"\t\t\t\t\ttext \"%s\"\n" +
									"\t\t\t\t\tdelayedA {\n" +
									"\t\t\t\t\t}\n" +
									"\t\t\t\t}\n\t\t\t\t", a.name, a.text));
						}
					}
				}
				if(card.moves!=null) {
					for (Move m : card.moves) {
						String movedesc = "";
						String movedamg = null;
						if(m.damage!=null){
							movedesc+=m.damage+" damage. ";
							movedamg=m.damage.replaceAll("[^\\d]","");
						}
						if(m.text!=null)
							movedesc+=m.text;

						String trailingString = "\n\t\t\t\t";
						if (card.moves.indexOf(m) == (card.moves.size() -1)) {
							trailingString = "";
						}
						moves.append(String.format("move \"%s\", {\n" +
								"\t\t\t\t\ttext \"%s\"\n" +
								"\t\t\t\t\tenergyCost %s\n" +
								"\t\t\t\t\tattackRequirement {}\n" +
								"\t\t\t\t\tonAttack {\n" +
								"\t\t\t\t\t\t%s\n" +
								"\t\t\t\t\t}\n" +
								"\t\t\t\t}%s", m.name, movedesc, StringUtils.join(m.cost,", "),movedamg!=null?"damage "+movedamg:"", trailingString));

					}
				}
			}

			String impl=null;
			if (cardTypeSet.contains("BABY")) {
				impl =  String.format("baby (this, successors:%s, hp:%s, type:%s, retreatCost:%s) {\n" +
						"\t\t\t\t%s%s%s%s\n" +
						"\t\t\t}",
						"'SUCCESSOR(S)'", hp, typesCombined, rc, weakness.toString(), resistance.toString(), abilities.toString(), moves.toString());
			}
			else if (cardTypeSet.contains("BASIC") || cardTypeSet.contains("RESTORED")) {
				impl =  String.format("basic (this, hp:%s, type:%s, retreatCost:%s) {\n" +
						"\t\t\t\t%s%s%s%s\n" +
						"\t\t\t}",
						hp, typesCombined, rc, weakness.toString(), resistance.toString(), abilities.toString(), moves.toString());
			}
			else if (cardTypeSet.contains("EVOLUTION") || cardTypeSet.contains("VMAX")) {
				impl =  String.format("evolution (this, from:\"%s\", hp:%s, type:%s, retreatCost:%s) {\n" +
						"\t\t\t\t%s%s%s%s\n" +
						"\t\t\t}",
						predecessor, hp, typesCombined, rc, weakness.toString(), resistance.toString(), abilities.toString(), moves.toString());
			}
			else if (cardTypeSet.contains("SUPPORTER")) {
				impl =  String.format("supporter (this) {\n" +
						"\t\t\t\ttext \"%s\"\n" +
						"\t\t\t\tonPlay {\n" +
						"\t\t\t\t}\n" +
						"\t\t\t\tplayRequirement{\n" +
						"\t\t\t\t}\n" +
						"\t\t\t}", cardtext);
			}
			else if (cardTypeSet.contains("STADIUM")){
				impl =  String.format("stadium (this) {\n" +
						"\t\t\t\ttext \"%s\"\n" +
						"\t\t\t\tonPlay {\n" +
						"\t\t\t\t}\n" +
						"\t\t\t\tonRemoveFromPlay{\n" +
						"\t\t\t\t}\n" +
						"\t\t\t}", cardtext);
			}
			else if (cardTypeSet.contains("POKEMON_TOOL")){
				impl =  String.format("pokemonTool (this) {\n" +
						"\t\t\t\ttext \"%s\"\n" +
						"\t\t\t\tonPlay {reason->\n" +
						"\t\t\t\t}\n" +
						"\t\t\t\tonRemoveFromPlay {\n" +
						"\t\t\t\t}\n" +
						"\t\t\t\tallowAttach {to->\n" +
						"\t\t\t\t}\n" +
						"\t\t\t}", cardtext);
			}
			else if (cardTypeSet.contains("ITEM")) {
				impl =  String.format("itemCard (this) {\n" +
								"\t\t\t\ttext \"%s\"\n" +
								"\t\t\t\tonPlay {\n" +
								"\t\t\t\t}\n" +
								"\t\t\t\tplayRequirement{\n" +
								"\t\t\t\t}\n" +
								"\t\t\t}", cardtext);
			}
			else if (cardTypeSet.contains("TRAINER")) {
				impl =  String.format("basicTrainer (this) {\n" +
						"\t\t\t\ttext \"%s\"\n" +
						"\t\t\t\tonPlay {\n" +
						"\t\t\t\t}\n" +
						"\t\t\t\tplayRequirement{\n" +
						"\t\t\t\t}\n" +
						"\t\t\t}", cardtext);
			}
			else if (cardTypeSet.contains("SPECIAL_ENERGY")) {
				impl =  String.format("specialEnergy (this, [[C]]) {\n" +
						"\t\t\t\ttext \"%s\"\n" +
						"\t\t\t\tonPlay {reason->\n" +
						"\t\t\t\t}\n" +
						"\t\t\t\tonRemoveFromPlay {\n" +
						"\t\t\t\t}\n" +
						"\t\t\t\tonMove {to->\n" +
						"\t\t\t\t}\n" +
						"\t\t\t\tallowAttach {to->\n" +
						"\t\t\t\t}\n" +
						"\t\t\t}", cardtext);
			}
			else if (cardTypeSet.contains("BASIC_ENERGY")) {
				impl =  String.format("basicEnergy (this, %s)", card.energy.get(0).get(0));
			}
			if(impl == null){
				throw new IllegalStateException("Impl null:"+card.name+","+card.number);
			}

			if(card.variantOf != null){
				//search for reprints in same set
				for(List2Item list2Item : list2){
					if(list2Item.getId().equals(card.variantOf)){
						impl = String.format("copy (%s, this)", list2Item.name);
						System.out.println("REPRINT_SAME "+ list2Item.name);
						break;
					}
				}
			}

			List1Item item1 = new List1Item();
			item1.cardtype=cardTypeSet;
			item1.cardNumber = card.number;
			item1.fullname = card.name;
			item1.name = card.enumId;
			item1.rarity = rarity;
			list1.add(item1);
			List2Item item2 = new List2Item();
			item2.id = card.id;
			item2.name = card.enumId;
			item2.impl = impl;
			list2.add(item2);
		}

		try {
			Properties properties = new Properties();
			properties.setProperty("resource.loader", "class");
			properties.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
			properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			Velocity.init(properties);
			VelocityContext context = new VelocityContext(modelmap);
			Writer writer = new FileWriter(new File(String.format("impl/%s.groovy", modelmap.get("classname"))));
			Velocity.mergeTemplate("set2.vm", "UTF-8", context, writer);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class List1Item {
		private String name;
		private String fullname;
		private List<String> cardtype;
		private String rarity;
		private String cardNumber;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getFullname() {
			return fullname;
		}
		public void setFullname(String fullname) {
			this.fullname = fullname;
		}
		public List<String> getCardtype() {
			return cardtype;
		}
		public void setCardtype(List<String> cardtype) {
			this.cardtype = cardtype;
		}
		public String getRarity() {
			return rarity;
		}
		public void setRarity(String rarity) {
			this.rarity = rarity;
		}
		public String getCardNumber() {
			return cardNumber;
		}
		public void setCardNumber(String cardNumber) {
			this.cardNumber = cardNumber;
		}

	}

	public static class List2Item {
		private String id;
		private String name;
		private String impl;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getImpl() {
			return impl;
		}
		public void setImpl(String impl) {
			this.impl = impl;
		}

	}
}
