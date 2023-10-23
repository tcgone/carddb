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

  public static class ClassificationsImporter extends Importer {
    public ClassificationsImporter() throws Exception {
      process();
    }

    void export(String setEnumId) {
//      System.out.println(setEnumId);
      allCards.stream()
        .filter(card -> card.getExpansion().getEnumId().equals(setEnumId))
        .map(card -> {
          List<String> list = new ArrayList<>();
          list.add(card.getName());
          list.add(card.getExpansion().getName());
          list.add(card.getNumber());
          list.add(card.getRarity().toString());
          list.add(""); // class
          list.add(""); // freebies
          list.add("KIT"); // KIT
          list.add(card.getScanUrl()); // scan url
          return String.join("\t", list);
        })
        .forEach(line -> System.out.println(line));
    }
  }
  public void export() throws Exception {
    ClassificationsImporter process = new ClassificationsImporter();
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
