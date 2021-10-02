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
        .replace("rare holo vmax", "Rare Holo")
        .replace("rare holo v", "Rare Holo")
        .replace("rare rainbow", "Ultra Rare");

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

  private Set<String> stage1Db = new HashSet<>();
  private Map<String, Expansion> setMap = new HashMap<>();

  private Card prepareCard(PioCard pc) {
    Card c = new Card();
    c.name=pc.name;
    c.pioId=pc.id;
    c.number=pc.number;
    c.artist=pc.artist;
    if(pc.text!=null)c.text=pc.text.stream().map(this::replaceTypesWithShortForms).flatMap(x->Arrays.stream(x.split("\\\\n"))).filter(s->!s.trim().isEmpty()).collect(Collectors.toList());
    c.rarity= Rarity.of(pc.rarity);
    if(!setMap.containsKey(pc.setCode)){
      log.warn("PLEASE FILL IN id, abbr, enumId FIELDS in {}", pc.set);
      Expansion expansion = new Expansion();
      expansion.name=pc.set;
      expansion.id="FILL_THIS";
      expansion.abbr="FILL_THIS";
      expansion.enumId="FILL_THIS";
      expansion.pioId=pc.setCode;
      setMap.put(pc.setCode, expansion);
    }
    Expansion expansion = setMap.get(pc.setCode);
    c.expansion = expansion;
    c.enumId=String.format("%s_%s", pc.name
      .replace("–","-").replace("’","'").toUpperCase(Locale.ENGLISH)
      .replaceAll("[ \\p{Punct}]", "_").replaceAll("_+","_").replace("É", "E"), pc.number);
    c.id=String.format("%s-%s", expansion.id, pc.number);
    c.subTypes=new ArrayList<>();

    switch (pc.supertype){
      case "Pokémon":
        c.superType= CardType.POKEMON;
        // hp of one side of legend cards is null
        if(pc.hp!=null) c.hp= Integer.valueOf(pc.hp);
        c.retreatCost=pc.convertedRetreatCost;
        if(pc.resistances!=null&&!pc.resistances.isEmpty()) c.resistances=pc.resistances.stream().peek(wr -> {
          wr.value = sanitizeCross(wr.value);
        }).collect(Collectors.toList());
        if(pc.weaknesses!=null&&!pc.weaknesses.isEmpty()) c.weaknesses=pc.weaknesses.stream().peek(wr -> {
          wr.value = sanitizeCross(wr.value);
        }).collect(Collectors.toList());
        if(pc.attacks!=null&&!pc.attacks.isEmpty()) c.moves=pc.attacks.stream().peek(a -> {
          a.damage=sanitizeCross(a.damage);
          a.text=replaceTypesWithShortForms(a.text);
        }).collect(Collectors.toList());
        if(pc.abilities != null) {
          pc.ability = pc.abilities.get(0);
        }
        if(pc.ability!=null){
          if(c.abilities==null)c.abilities=new ArrayList<>();
          Ability a=new Ability();
          a.type=pc.ability.type;
          a.name=pc.ability.name;
          a.text=replaceTypesWithShortForms(pc.ability.text);
          c.abilities.add(a);
        }
        if(pc.ancientTrait!=null){
          if(c.abilities==null)c.abilities=new ArrayList<>();
          Ability a=new Ability();
          a.type=pc.ancientTrait.type;
          a.name=pc.ancientTrait.name;
          a.text=replaceTypesWithShortForms(pc.ancientTrait.text);
          c.abilities.add(a);
        }
        c.types=pc.types;
        c.nationalPokedexNumber=pc.nationalPokedexNumber;
        c.evolvesFrom= StringUtils.trimToNull(pc.evolvesFrom);
        c.evolvesTo=pc.evolvesTo;
        break;
      case "Trainer":
        c.superType=TRAINER;
        break;
      case "Energy":
        c.superType=ENERGY;
        if(c.text!=null&&!c.text.isEmpty()){
          c.subTypes.add(SPECIAL_ENERGY);
        } else {
          c.subTypes.add(BASIC_ENERGY);
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
          c.subTypes.add(LEGEND);
          break;
        case "Basic":
          if (pc.supertype.equals("Energy")) break;
          c.subTypes.add(BASIC);
          if (pc.name.contains("-GX")) {
            c.subTypes.add(POKEMON_GX);
            if (pc.name.contains(" & ")) {
              c.subTypes.add(TAG_TEAM);
            }
          }
          if (pc.name.contains("-EX")) {
            c.subTypes.add(POKEMON_EX);
          }
          if (pc.name.endsWith("V")) {
            c.subTypes.add(POKEMON_V);
          }
          break;
        case "Stage 1":
          c.subTypes.add(EVOLUTION);
          c.subTypes.add(STAGE1);
          if (pc.name.contains("-GX")) {
            c.subTypes.add(POKEMON_GX);
            if (pc.name.contains(" & ")) {
              c.subTypes.add(TAG_TEAM);
            }
          }
          if (pc.name.contains("-EX")) {
            c.subTypes.add(POKEMON_EX);
          }
          stage1Db.add(pc.name);
          break;
        case "Stage 2":
          c.subTypes.add(EVOLUTION);
          c.subTypes.add(STAGE2);
          if (pc.name.contains("-GX")) {
            c.subTypes.add(POKEMON_GX);
            if (pc.name.contains(" & ")) {
              c.subTypes.add(TAG_TEAM);
            }
          }
          if (pc.name.contains("-EX")) {
            c.subTypes.add(POKEMON_EX);
          }
          break;
        case "GX":
          c.subTypes.add(BASIC);
          c.subTypes.add(POKEMON_GX);
          if (pc.name.contains(" & ")) {
            c.subTypes.add(TAG_TEAM);
          }
          break;
        case "EX":
          c.subTypes.add(pc.name.endsWith(" ex") ? EX : POKEMON_EX);
          if (StringUtils.isNotBlank(pc.evolvesFrom)) {
            c.subTypes.add(stage1Db.contains(pc.evolvesFrom)
              ? STAGE2 : STAGE1);
            c.subTypes.add(EVOLUTION);
          } else {
            c.subTypes.add(BASIC);
          }
          break;
        case "VMAX":
          c.subTypes.add(VMAX);
          c.subTypes.add(EVOLUTION);
          break;
        case "MEGA":
          c.subTypes.add(EVOLUTION);
          c.subTypes.add(MEGA_POKEMON);
          c.subTypes.add(POKEMON_EX);
          break;
        case "BREAK":
          c.subTypes.add(EVOLUTION);
          c.subTypes.add(BREAK);
          break;
        case "Level Up":
          c.subTypes.add(EVOLUTION);
          c.subTypes.add(LVL_X);
          break;
        case "Restored":
          c.subTypes.add(RESTORED);
          break;
        case "Stadium":
          c.subTypes.add(STADIUM);
          break;
        case "Item":
          c.subTypes.add(ITEM);
          break;
        case "Pokémon Tool":
          c.subTypes.add(POKEMON_TOOL);
//				if(modernSeries.contains(pc.series)){
          c.subTypes.add(ITEM);
//				}
          break;
        case "Rocket's Secret Machine":
          c.subTypes.add(ROCKETS_SECRET_MACHINE);
          break;
        case "Technical Machine":
          c.subTypes.add(TECHNICAL_MACHINE);
          break;
        case "Supporter":
          c.subTypes.add(SUPPORTER);
          break;
        case "Single Strike":
          c.subTypes.add(SINGLE_STRIKE);
          break;
        case "Rapid Strike":
          c.subTypes.add(RAPID_STRIKE);
        case "": // basic trainer
          break;
      }
    }
    Collections.sort(c.subTypes);
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
