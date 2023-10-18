package tcgone.carddb.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import tcgone.carddb.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

import static tcgone.carddb.model.CardType.*;

/**
 * @author axpendix@hotmail.com
 */
public class PioReader {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PioReader.class);

  public static String getFilenameWithoutExtension(String filenameWithExtension) {
    return filenameWithExtension.replaceFirst("[.][^.]+$", "");
  }

  public List<ExpansionFile> load(Stack<File> files) throws IOException {
    List<ExpansionFile> result = new ArrayList<>();
    for (File file : files) {
      String expansionPioId = getFilenameWithoutExtension(file.getName());
      Expansion expansion = setMap.get(expansionPioId);
      log.info("Loading {}", file.getPath());
      if (expansion == null) {
        throw new IllegalStateException(String.format("Expansion with pioId '%s' not found. Have you loaded pio expansions file?", expansionPioId));
      }
      List<Card> cards = new ArrayList<>();
      try (InputStream inputStream = Files.newInputStream(file.toPath())) {
        ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        List<PioCardV1> list = mapper.readValue(inputStream, new TypeReference<List<PioCardV1>>(){});
        for (PioCardV1 pc : list) {
          log.info("Reading {} {}", pc.name, pc.number);
          validatePioCard(pc);

          Card card1 = prepareCard(pc);
          cards.add(card1);
        }
      }
      result.add(new ExpansionFile("E3", expansion, cards));
    }
    return result;
  }

  private static void validatePioCard(PioCardV1 pc) {
    if(pc.rarity==null){
      throw new IllegalStateException("rarity cannot be null");
    }
    pc.rarity= pc.rarity
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
      log.warn("NULL TYPES for "+ pc.id+", "+ pc.name);
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

  }

  private Map<String, Expansion> setMap = new HashMap<>();

  public void loadExpansions(String[] files) throws IOException {
    for (String file : files) {
      log.info("Loading {}", file);
      try (InputStream inputStream = Files.newInputStream(Paths.get(file))) {
        ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        List<PioSetV1> pioSets = mapper.readValue(inputStream, new TypeReference<List<PioSetV1>>(){});

        for (PioSetV1 ps : pioSets) {
          Expansion expansion = new Expansion();
          expansion.setName(ps.name);
          expansion.setShortName(ps.ptcgoCode);
          expansion.setEnumId(ps.name.replace("–", "-").replace("’", "'").toUpperCase(Locale.ENGLISH)
            .replaceAll("[ \\p{Punct}]", "_").replaceAll("_+", "_").replace("É", "E"));
          expansion.setPioId(ps.id);
          expansion.setReleaseDate(ps.releaseDate);
          expansion.setSeries(ps.series);
          expansion.setOfficialCount(ps.printedTotal);
          setMap.put(expansion.getPioId(), expansion);
        }
      }
    }
  }

  private Set<String> stage1Db = new HashSet<>();

  private Card prepareCard(PioCardV1 pc) {
    Card c = new Card();
    c.setName(pc.name);
    c.setPioId(pc.id);
    c.setNumber(pc.number);
    c.setArtist(pc.artist);
    c.setRegulationMark(pc.regulationMark);
    if(pc.text!=null)
      c.setText(pc.text.stream().map(this::replaceTypesWithShortForms).flatMap(x->Arrays.stream(x.split("\\\\n"))).filter(s->!s.trim().isEmpty()).collect(Collectors.joining("\n")));
    else if(pc.rules!=null)
      c.setText(pc.rules.stream().map(this::replaceTypesWithShortForms).flatMap(x->Arrays.stream(x.split("\\\\n"))).filter(s->!s.trim().isEmpty()).collect(Collectors.joining("\n")));
    c.setRarity(Rarity.of(pc.rarity));
    if(!setMap.containsKey(pc.id.replace('-'+pc.number, ""))){
      log.warn("PLEASE FILL IN enumId & shortName FIELDS in {}", pc.id.replace('-'+pc.number, ""));
      Expansion expansion = new Expansion();
      expansion.setName(pc.id.replace('-'+pc.number, ""));
      expansion.setShortName("FILL_THIS");
      expansion.setEnumId("FILL_THIS");
      expansion.setPioId(pc.id.replace('-'+pc.number, ""));
      setMap.put(expansion.getPioId(), expansion);
    }
    Expansion expansion = setMap.get(pc.id.replace('-'+pc.number, ""));
    c.setEnumId(String.format("%s_%s", pc.name
      .replace("–","-").replace("’","'").toUpperCase(Locale.ENGLISH)
      .replaceAll("[ \\p{Punct}]", "_").replaceAll("_+","_").replace("É", "E"), pc.number));
    List<CardType> cardTypes=new ArrayList<>();
    c.setCardTypes(cardTypes);

    switch (pc.supertype){
      case "Pokémon":
        cardTypes.add(CardType.POKEMON);
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
        c.setEvolvesFrom(Collections.singletonList(StringUtils.trimToNull(pc.evolvesFrom)));
        break;
      case "Trainer":
        cardTypes.add(TRAINER);
        break;
      case "Energy":
        cardTypes.add(ENERGY);
        if(c.getText() !=null&&!c.getText().isEmpty()){
          cardTypes.add(SPECIAL_ENERGY);
        } else {
          cardTypes.add(BASIC_ENERGY);
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
          cardTypes.add(LEGEND);
          break;
        case "Basic":
          if (pc.supertype.equals("Energy")) break;
          cardTypes.add(BASIC);
          if (pc.name.contains("-GX")) {
            cardTypes.add(POKEMON_GX);
            if (pc.name.contains(" & ")) {
              cardTypes.add(TAG_TEAM);
            }
          }
          if (pc.name.contains("-EX")) {
            cardTypes.add(POKEMON_EX);
          }
          if (pc.name.endsWith("V")) {
            cardTypes.add(POKEMON_V);
          }
          break;
        case "Stage 1":
          cardTypes.add(EVOLUTION);
          cardTypes.add(STAGE1);
          if (pc.name.contains("-GX")) {
            cardTypes.add(POKEMON_GX);
            if (pc.name.contains(" & ")) {
              cardTypes.add(TAG_TEAM);
            }
          }
          if (pc.name.contains("-EX")) {
            cardTypes.add(POKEMON_EX);
          }
          stage1Db.add(pc.name);
          break;
        case "Stage 2":
          cardTypes.add(EVOLUTION);
          cardTypes.add(STAGE2);
          if (pc.name.contains("-GX")) {
            cardTypes.add(POKEMON_GX);
            if (pc.name.contains(" & ")) {
              cardTypes.add(TAG_TEAM);
            }
          }
          if (pc.name.contains("-EX")) {
            cardTypes.add(POKEMON_EX);
          }
          break;
        case "GX":
          cardTypes.add(BASIC);
          cardTypes.add(POKEMON_GX);
          if (pc.name.contains(" & ")) {
            cardTypes.add(TAG_TEAM);
          }
          break;
        case "EX":
          cardTypes.add(pc.name.endsWith(" ex") ? EX : POKEMON_EX);
          if (StringUtils.isNotBlank(pc.evolvesFrom)) {
            cardTypes.add(stage1Db.contains(pc.evolvesFrom)
              ? STAGE2 : STAGE1);
            cardTypes.add(EVOLUTION);
          } else {
            cardTypes.add(BASIC);
          }
          break;
        case "VMAX":
          cardTypes.add(VMAX);
          cardTypes.add(EVOLUTION);
          break;
        case "VSTAR":
          cardTypes.add(VSTAR);
          cardTypes.add(EVOLUTION);
        case "V-UNION":
          cardTypes.add(V_UNION);
        case "MEGA":
          cardTypes.add(EVOLUTION);
          cardTypes.add(MEGA_POKEMON);
          cardTypes.add(POKEMON_EX);
          break;
        case "BREAK":
          cardTypes.add(EVOLUTION);
          cardTypes.add(BREAK);
          break;
        case "Level Up":
        case "Level-Up":
          cardTypes.add(EVOLUTION);
          cardTypes.add(LVL_X);
          break;
        case "Restored":
          cardTypes.add(RESTORED);
          break;
        case "Stadium":
          cardTypes.add(STADIUM);
          break;
        case "Item":
          cardTypes.add(ITEM);
          break;
        case "Pokémon Tool":
          cardTypes.add(POKEMON_TOOL);
//				if(modernSeries.contains(pc.series)){
          cardTypes.add(ITEM);
//				}
          break;
        case "Rocket's Secret Machine":
          cardTypes.add(ROCKETS_SECRET_MACHINE);
          break;
        case "Technical Machine":
          cardTypes.add(TECHNICAL_MACHINE);
          break;
        case "Supporter":
          cardTypes.add(SUPPORTER);
          break;
        case "Single Strike":
          cardTypes.add(SINGLE_STRIKE);
          break;
        case "Rapid Strike":
          cardTypes.add(RAPID_STRIKE);
        case "": // basic trainer
          break;
      }
    }
    Collections.sort(cardTypes);
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
