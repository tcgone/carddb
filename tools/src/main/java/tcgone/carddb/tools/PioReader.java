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
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang3.StringUtils;
import tcgone.carddb.model.*;
import tcgone.carddb.tools.deserializers.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author axpendix@hotmail.com
 */
public class PioReader {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PioReader.class);

  public List<Card> load(InputStream inputStream) throws IOException {
    ObjectMapper mapper = new ObjectMapper()
      .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    SimpleModule cardModule = new SimpleModule();
    cardModule.addDeserializer(Card.class, new CardDeserializer());
    cardModule.addDeserializer(Rarity.class, new RarityDeserializer());
    cardModule.addDeserializer(CardType.class, new CardSuperTypeDeserializer());
    cardModule.addDeserializer(WeaknessResistance.class, new WeaknessResistanceDeserializer());
    cardModule.addDeserializer(Ability.class, new AbilityDeserializer());
    cardModule.addDeserializer(Move.class, new MoveDeserializer());
    mapper.registerModule(cardModule);
    List<Card> cards=new ArrayList<>();

    List<Card> list = mapper.readValue(inputStream, new TypeReference<List<Card>>(){});
    for (Card card : list) {
      log.info("Reading {} {}", card.name, card.number);
      cards.add(prepareCard(card));
    }
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
          expansion.name = ps.name;
          expansion.id = "FILL_THIS";
          expansion.abbr = ps.ptcgoCode;
          expansion.enumId = ps.name.replace("–", "-").replace("’", "'").toUpperCase(Locale.ENGLISH)
            .replaceAll("[ \\p{Punct}]", "_").replaceAll("_+", "_").replace("É", "E");
          expansion.pioId = ps.id;
        }
        log.warn("PLEASE FILL IN id FIELD in {}", expansion.name);
        setMap.put(expansion.pioId, expansion);
      }
    }
  }

  private Card prepareCard(Card pc) {
    if(!setMap.containsKey(pc.pioId.replace('-'+pc.number, ""))){
      log.warn("PLEASE FILL IN id, abbr, enumId FIELDS in {}", pc.pioId.replace('-'+pc.number, ""));
      Expansion expansion = new Expansion();
      expansion.name=pc.pioId.replace('-'+pc.number, "");
      expansion.id="FILL_THIS";
      expansion.abbr="FILL_THIS";
      expansion.enumId="FILL_THIS";
      expansion.pioId = pc.pioId.replace('-'+pc.number, "");
      setMap.put(expansion.pioId, expansion);
    }
    Expansion expansion = setMap.get(pc.pioId.replace('-'+pc.number, ""));
    pc.expansion = expansion;
    pc.id=String.format("%s-%s", expansion.id, pc.number);
    return pc;
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
