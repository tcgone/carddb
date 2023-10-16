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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import tcgone.carddb.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

import static tcgone.carddb.model.CardType.*;

/**
 * @author axpendix@hotmail.com
 */
public class PioReader {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PioReader.class);

  public List<Card> load(InputStream inputStream) throws IOException {
    ObjectMapper mapper = new ObjectMapper()
      .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
//        Resource[] resources = applicationContext.getResources("classpath:/pio/*.json");
    Map<String, Set<String>> superToSub = new LinkedHashMap<>();
    List<Card> cards=new ArrayList<>();

//
    List<PioCard> list = mapper.readValue(inputStream, new TypeReference<List<PioCard>>(){});
    for (PioCard pc : list) {
      log.info("Reading {} {}", pc.name, pc.number);
      if(pc.rarity==null){
        throw new IllegalStateException("rarity cannot be null");
      }
      pc.rarity=pc.rarity
        .toLowerCase(Locale.ENGLISH)
        .replace("rare secret","Secret")
        .replace("rare ace","Rare")
        .replace("rare holo lv.x","Rare Holo")
        .replace("rare ultra","Ultra Rare")
        .replace("rareultra","Ultra Rare")
        .replace("rare prime","Rare")
        .replace("rare break","Ultra Rare")
        .replace("rare holo ex","Ultra Rare")
        .replace("rare holo gx","Ultra Rare")
        .replace("rare promo","Promo")
        .replace("legend","Ultra Rare")
        .replace("rareholovmax", "Rare Holo")
        .replace("rareholov","Rare Holo")
        .replace("rare holo vstar", "Rare Holo")
        .replace("rare holo vmax", "Rare Holo")
        .replace("rare holo v", "Rare Holo")
        .replace("rare rainbow", "Ultra Rare")
        .replace("amazing rare", "Rare Holo")
        .replace("radiant rare", "Rare")
        .replace("rare shiny", "Rare Holo")
        .replace("classic collection", "Ultra Rare")
        .replace("vm", "Rare Holo")
        .replace("v", "Rare Holo");

      pc.rarity= WordUtils.capitalizeFully(pc.rarity);

      if(pc.supertype.equals("Pokémon") && pc.types == null){
        log.warn("NULL TYPES for "+pc.id+", "+pc.name);
      }

//                if(pc.supertype.equals("Pokémon")){
//                    try {
//                        Integer.parseInt(pc.hp);
//                    } catch (Exception e) {
//                        log.warn("No HP for "+pc.id+", "+pc.name);
//                    }
//                }
//                if(pc.subtype.equals("Level Up")){
//                    log.warn("Level Up. name:{}, level:{}", pc.name, pc.level);
//                }

      if(!superToSub.containsKey(pc.supertype))
        superToSub.put(pc.supertype,new HashSet<>());
      superToSub.get(pc.supertype).add(pc.subtype);

      Card c1 = prepareCard(pc);
      cards.add(c1);

    }
//		log.info("superToSub: /{}/", superToSub);
    inputStream.close();
    return cards;
  }

  private Map<String, Expansion> setMap = new HashMap<>();

  public void loadExpansions(InputStream inputStream, List<String> expansionIds) throws IOException {
    ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    List<PioSet> list = mapper.readValue(inputStream, new TypeReference<List<PioSet>>(){});

    for (String expansionId : expansionIds) {
      if (!setMap.containsKey(expansionId)) {
        Expansion expansion = new Expansion();
        for (PioSet ps : list) {
          if (!Objects.equals(ps.id, expansionId)) continue;
          expansion.setName(ps.name);
          expansion.setId("FILL_THIS");
          expansion.setAbbr(ps.ptcgoCode);
          expansion.setEnumId(ps.name.replace("–", "-").replace("’", "'").toUpperCase(Locale.ENGLISH)
            .replaceAll("[ \\p{Punct}]", "_").replaceAll("_+", "_").replace("É", "E"));
          expansion.setPioId(ps.id);
        }
        log.warn("PLEASE FILL IN id FIELD in {}", expansion.getName());
        setMap.put(expansion.getPioId(), expansion);
      }
    }
  }

  private Set<String> stage1Db = new HashSet<>();

  private Card prepareCard(PioCard pc) {
    Card c = new Card();
    c.setName(pc.name);
    c.setPioId(pc.id);
    c.setNumber(pc.number);
    c.setArtist(pc.artist);
    c.setRegulationMark(pc.regulationMark);
    if(pc.text!=null)
      c.setText(pc.text.stream().map(this::replaceTypesWithShortForms).flatMap(x->Arrays.stream(x.split("\\\\n"))).filter(s->!s.trim().isEmpty()).collect(Collectors.toList()));
    else if(pc.rules!=null)
      c.setText(pc.rules.stream().map(this::replaceTypesWithShortForms).flatMap(x->Arrays.stream(x.split("\\\\n"))).filter(s->!s.trim().isEmpty()).collect(Collectors.toList()));
    c.setRarity(Rarity.of(pc.rarity));
    if(!setMap.containsKey(pc.id.replace('-'+pc.number, ""))){
      log.warn("PLEASE FILL IN id, abbr, enumId FIELDS in {}", pc.id.replace('-'+pc.number, ""));
      Expansion expansion = new Expansion();
      expansion.setName(pc.id.replace('-'+pc.number, ""));
      expansion.setId("FILL_THIS");
      expansion.setAbbr("FILL_THIS");
      expansion.setEnumId("FILL_THIS");
      expansion.setPioId(pc.id.replace('-'+pc.number, ""));
      setMap.put(expansion.getPioId(), expansion);
    }
    Expansion expansion = setMap.get(pc.id.replace('-'+pc.number, ""));
    c.setExpansion(expansion);
    c.setEnumId(String.format("%s_%s", pc.name
      .replace("–","-").replace("’","'").toUpperCase(Locale.ENGLISH)
      .replaceAll("[ \\p{Punct}]", "_").replaceAll("_+","_").replace("É", "E"), pc.number));
    c.setId(String.format("%s-%s", expansion.getId(), pc.number));
    c.setSubTypes(new ArrayList<>());

    switch (pc.supertype){
      case "Pokémon":
        c.setSuperType(CardType.POKEMON);
        // hp of one side of legend cards is null
        if(pc.hp!=null) c.setHp(Integer.valueOf(pc.hp));
        c.setRetreatCost(pc.convertedRetreatCost);
        if(pc.resistances!=null&&!pc.resistances.isEmpty()) c.setResistances(pc.resistances.stream().peek(wr -> {
          wr.setValue(sanitizeCross(wr.getValue()));
        }).collect(Collectors.toList()));
        if(pc.weaknesses!=null&&!pc.weaknesses.isEmpty()) c.setWeaknesses(pc.weaknesses.stream().peek(wr -> {
          wr.setValue(sanitizeCross(wr.getValue()));
        }).collect(Collectors.toList()));
        if(pc.attacks!=null&&!pc.attacks.isEmpty()) c.setMoves(pc.attacks.stream().peek(a -> {
          a.setDamage(sanitizeCross(a.getDamage()));
          a.setText(replaceTypesWithShortForms(a.getText()));
        }).collect(Collectors.toList()));
        if(pc.abilities != null) {
          pc.ability = pc.abilities.get(0);
        }
        if(pc.ability!=null){
          if(c.getAbilities() ==null) c.setAbilities(new ArrayList<>());
          Ability a=new Ability();
          a.setType(pc.ability.getType());
          a.setName(pc.ability.getName());
          a.setText(replaceTypesWithShortForms(pc.ability.getText()));
          c.getAbilities().add(a);
        }
        if(pc.ancientTrait!=null){
          if(c.getAbilities() ==null) c.setAbilities(new ArrayList<>());
          Ability a=new Ability();
          a.setType(pc.ancientTrait.getType());
          a.setName(pc.ancientTrait.getName());
          a.setText(replaceTypesWithShortForms(pc.ancientTrait.getText()));
          c.getAbilities().add(a);
        }
        c.setTypes(pc.types);
        c.setNationalPokedexNumber(pc.nationalPokedexNumber);
        c.setEvolvesFrom(StringUtils.trimToNull(pc.evolvesFrom));
        c.setEvolvesTo(pc.evolvesTo);
        break;
      case "Trainer":
        c.setSuperType(TRAINER);
        break;
      case "Energy":
        c.setSuperType(ENERGY);
        if(c.getText() !=null&&!c.getText().isEmpty()){
          c.getSubTypes().add(SPECIAL_ENERGY);
        } else {
          c.getSubTypes().add(BASIC_ENERGY);
          c.setEnergy(new ArrayList<>(Collections.singletonList(sanitizeType(Collections.singletonList(c.getName().split(" ")[0])))));
        }
        break;
    }
    if (pc.subtypes == null) {
      pc.subtypes = new ArrayList<>();
      pc.subtypes.add(pc.subtype);
    }
    for (String subtype : pc.subtypes) {
      if (subtype == null) break;
      switch (subtype) {
        case "LEGEND":
          c.getSubTypes().add(LEGEND);
          break;
        case "Basic":
          if (pc.supertype.equals("Energy")) break;
          c.getSubTypes().add(BASIC);
          if (pc.name.contains("-GX")) {
            c.getSubTypes().add(POKEMON_GX);
            if (pc.name.contains(" & ")) {
              c.getSubTypes().add(TAG_TEAM);
            }
          }
          if (pc.name.contains("-EX")) {
            c.getSubTypes().add(POKEMON_EX);
          }
          if (pc.name.endsWith("V")) {
            c.getSubTypes().add(POKEMON_V);
          }
          break;
        case "Stage 1":
          c.getSubTypes().add(EVOLUTION);
          c.getSubTypes().add(STAGE1);
          if (pc.name.contains("-GX")) {
            c.getSubTypes().add(POKEMON_GX);
            if (pc.name.contains(" & ")) {
              c.getSubTypes().add(TAG_TEAM);
            }
          }
          if (pc.name.contains("-EX")) {
            c.getSubTypes().add(POKEMON_EX);
          }
          stage1Db.add(pc.name);
          break;
        case "Stage 2":
          c.getSubTypes().add(EVOLUTION);
          c.getSubTypes().add(STAGE2);
          if (pc.name.contains("-GX")) {
            c.getSubTypes().add(POKEMON_GX);
            if (pc.name.contains(" & ")) {
              c.getSubTypes().add(TAG_TEAM);
            }
          }
          if (pc.name.contains("-EX")) {
            c.getSubTypes().add(POKEMON_EX);
          }
          break;
        case "GX":
          c.getSubTypes().add(BASIC);
          c.getSubTypes().add(POKEMON_GX);
          if (pc.name.contains(" & ")) {
            c.getSubTypes().add(TAG_TEAM);
          }
          break;
        case "EX":
          c.getSubTypes().add(pc.name.endsWith(" ex") ? EX : POKEMON_EX);
          if (StringUtils.isNotBlank(pc.evolvesFrom)) {
            c.getSubTypes().add(stage1Db.contains(pc.evolvesFrom)
              ? STAGE2 : STAGE1);
            c.getSubTypes().add(EVOLUTION);
          } else {
            c.getSubTypes().add(BASIC);
          }
          break;
        case "VMAX":
          c.getSubTypes().add(VMAX);
          c.getSubTypes().add(EVOLUTION);
          break;
        case "VSTAR":
          c.getSubTypes().add(VSTAR);
          c.getSubTypes().add(EVOLUTION);
        case "V-UNION":
          c.getSubTypes().add(V_UNION);
        case "MEGA":
          c.getSubTypes().add(EVOLUTION);
          c.getSubTypes().add(MEGA_POKEMON);
          c.getSubTypes().add(POKEMON_EX);
          break;
        case "BREAK":
          c.getSubTypes().add(EVOLUTION);
          c.getSubTypes().add(BREAK);
          break;
        case "Level Up":
        case "Level-Up":
          c.getSubTypes().add(EVOLUTION);
          c.getSubTypes().add(LVL_X);
          break;
        case "Restored":
          c.getSubTypes().add(RESTORED);
          break;
        case "Stadium":
          c.getSubTypes().add(STADIUM);
          break;
        case "Item":
          c.getSubTypes().add(ITEM);
          break;
        case "Pokémon Tool":
          c.getSubTypes().add(POKEMON_TOOL);
//				if(modernSeries.contains(pc.series)){
          c.getSubTypes().add(ITEM);
//				}
          break;
        case "Rocket's Secret Machine":
          c.getSubTypes().add(ROCKETS_SECRET_MACHINE);
          break;
        case "Technical Machine":
          c.getSubTypes().add(TECHNICAL_MACHINE);
          break;
        case "Supporter":
          c.getSubTypes().add(SUPPORTER);
          break;
        case "Single Strike":
          c.getSubTypes().add(SINGLE_STRIKE);
          break;
        case "Rapid Strike":
          c.getSubTypes().add(RAPID_STRIKE);
        case "": // basic trainer
          break;
      }
    }
    Collections.sort(c.getSubTypes());
    return c;
  }

  private String sanitizeCross(String s){
    if(s==null)return null;
    return s.replace("×","x");
  }
  private List<Type> sanitizeType(List<String> types){
    if(types==null) return null;
    return types.stream().map(Type::of).collect(Collectors.toList());
  }
  private String replaceTypesWithShortForms(String s){
    if(s==null)return null;
    return s
      .replace("{F}","[F]")
      .replace("{L}","[L]")
      .replace("{R}","[R]")
      .replace("{G}","[G]")
      .replace("{W}","[W]")
      .replace("{P}","[P]")
      .replace("{C}","[C]")
      .replace("{D}","[D]")
      .replace("{M}","[M]")
      .replace("{Y}","[Y]")
      .replace("{N}","[N]")
      .replace("Fighting Energy", "[F] Energy")
      .replace("Lightning Energy", "[L] Energy")
      .replace("Fire Energy", "[R] Energy")
      .replace("Grass Energy", "[G] Energy")
      .replace("Water Energy", "[W] Energy")
      .replace("Psychic Energy", "[P] Energy")
      .replace("Colorless Energy", "[C] Energy")
      .replace("Darkness Energy", "[D] Energy")
      .replace("Metal Energy", "[M] Energy")
      .replace("Fairy Energy", "[Y] Energy")
      .replace("Dragon Energy", "[N] Energy")
      .replace("Fighting Pokémon", "[F] Pokémon")
      .replace("Lightning Pokémon", "[L] Pokémon")
      .replace("Fire Pokémon", "[R] Pokémon")
      .replace("Grass Pokémon", "[G] Pokémon")
      .replace("Water Pokémon", "[W] Pokémon")
      .replace("Psychic Pokémon", "[P] Pokémon")
      .replace("Colorless Pokémon", "[C] Pokémon")
      .replace("Darkness Pokémon", "[D] Pokémon")
      .replace("Metal Pokémon", "[M] Pokémon")
      .replace("Fairy Pokémon", "[Y] Pokémon")
      .replace("Dragon Pokémon", "[N] Pokémon")
      .replace("Colorless", "[C]")
      .replace("Pokemon","Pokémon")
      .replace("`","'")
      .replace("–","-")
      ;
  }
  private <T> T diff(String context, T new1, T old1){
    if(!Objects.equals(new1, old1)){
      if(old1 instanceof String && new1 instanceof String){
        String old2= StringUtils.trimToNull((String) old1);
        String new2= StringUtils.trimToNull((String) new1);
        if(old2==null && new2==null) return null;
        if(old2==null) return (T) new2;
        if(new2==null) return (T) old2;
        return (T) pickPicker(context, new2, old2);
      }
      if(context.endsWith("evolvesTo")||context.endsWith("/text")){
        if(old1==null) return new1;
        if(new1==null) return old1;
      } else {
        if(old1==null && !(new1 instanceof java.util.Collection)) return new1;
        if(new1==null && !(old1 instanceof java.util.Collection)) return old1;
      }
      return pickPicker(context, new1, old1);
    }
    return old1;
  }
  private Scanner scanner;
  private String askAndGet(String ask){
    if(scanner==null)scanner=new Scanner(System.in);
    System.out.println(ask);
    return scanner.nextLine();
  }
  private <T> T pickPicker(String context, T p1, T p2){
    return pick(context,p1,p2);
  }
  private <T> T pick(String context, T p1, T p2){
    if(scanner==null)scanner=new Scanner(System.in);
    while (true){
      if(p1 instanceof String && p2 instanceof String){
        System.out.println(context+". Pick one (1, 2 or 3 to enter your own)");
        System.out.println("\t1. "+p1);
        System.out.println("\t2. "+p2);
        try {
          String s = scanner.nextLine();
          int i = Integer.parseInt(s);
          if(i==1) return p1;
          if(i==2) return p2;
          if(i==3) {
            String line = askAndGet("Enter (blank to return back to selection)");
            if(!line.isEmpty()) return (T) line;
          }
        } catch (Exception e) {
        }
      } else {
        System.out.println(context+". Pick one (1, 2)");
        System.out.println("\t1. "+p1);
        System.out.println("\t2. "+p2);
        try {
          String s = scanner.nextLine();
          int i = Integer.parseInt(s);
          if(i==1) return p1;
          if(i==2) return p2;
        } catch (Exception e) {
        }
      }
    }
  }
}
