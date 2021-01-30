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

import org.junit.Ignore;

import java.util.ArrayList;
import java.util.List;

/**
 * Exports classification list in CSV format to be used for career pack classifications
 *
 * @author axpendix@hotmail.com
 */
@Ignore
public class ClassificationsExportTest {

  public class ClassificationsImporter extends Importer {
    void export(String setEnumId) {
//      System.out.println(setEnumId);
      allCards.stream()
        .filter(card -> card.set.enumId.equals(setEnumId))
        .map(card -> {
          List<String> list = new ArrayList<>();
          list.add(card.name);
          list.add(card.set.name);
          list.add(card.number);
          list.add(card.rarity.toString());
          list.add(""); // class
          list.add(""); // freebies
          list.add("KIT"); // KIT
          list.add(card.imageUrlHiRes); // scan url
          return String.join("\t", list);
        })
        .forEach(line -> System.out.println(line));
    }
  }
  public void export() throws Exception {
    ClassificationsImporter process = new ClassificationsImporter();
    process.init();
    process.export("FIRERED_LEAFGREEN");
    process.export("TEAM_ROCKET_RETURNS");
    process.export("DEOXYS");
    process.export("EMERALD");
    process.export("UNSEEN_FORCES");
    process.export("DELTA_SPECIES");
    process.export("LEGEND_MAKER");
    process.export("HOLON_PHANTOMS");
    process.export("CRYSTAL_GUARDIANS");
    process.export("DRAGON_FRONTIERS");
    process.export("POWER_KEEPERS");
  }
}
