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
package tcgone.carddb.data;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import tcgone.carddb.model.Card;
import tcgone.carddb.model.Expansion;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author axpendix@hotmail.com
 */
public class ImportTest {

  private static Importer importer;
  @BeforeClass
  public static void testImport() throws Exception {
    importer = new Importer();
    importer.init();
  }
  @Test
  public void printNonIntegerNumberedCards() {
    Map<String, Collection<String>> mmap = new HashMap<>();
    for (Card card : importer.getAllCards()) {
      if(!NumberUtils.isDigits(card.getNumber())){
        mmap.computeIfAbsent(card.getExpansion().getEnumId(), k -> new ArrayList<>()).add(card.getEnumId());
      }
    }
//    for (Map.Entry<String, Collection<String>> entry : mmap.entrySet()) {
//      System.out.println(entry.getKey() + ":" + entry.getValue());
//    }
  }
  @Test
  public void outputSomeHoloCards() {
    // print AQP and SKR holo rares to be added to career pack card pool
    for (Expansion expansion : importer.allExpansions) {
      if (expansion.getAbbr().equals("AQP") || expansion.getAbbr().equals("SKR")){
        for (Card c : expansion.getCards()) {
          if (c.getNumber().startsWith("H")) {
            String holoOf = "";
            List<Card> eqCards = new ArrayList<>();
            for (Card c1 : expansion.getCards()) {
              if (c1 != c && c1.getName().equals(c.getName())) {
                eqCards.add(c1);
              }
            }
            if(eqCards.size() == 1) {
              holoOf= eqCards.get(0).getNumber();
            } else if(eqCards.size()>1){
              holoOf=eqCards.stream().map(card -> card.getNumber()).collect(Collectors.joining("///"));
            }
            System.out.println(String.join(",", c.getName(), c.getExpansion().getName(), c.getNumber(),
              c.getRarity().toString(), "Holo", "",
              "", holoOf));
          }
        }
      }
    }
  }
}
