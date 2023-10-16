package tcgone.carddb.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import tcgone.carddb.model.Card;
import tcgone.carddb.model.Expansion;
import tcgone.carddb.model3.Card3;
import tcgone.carddb.model3.ExpansionFile3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author axpendix@hotmail.com
 */
public class Application {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) throws Exception {
    new Application(args);
  }

  private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
  private final PioReader pioReader=new PioReader();
  private final Options options;

  public Application(String[] args) throws Exception {

    System.setProperty("java.net.useSystemProxies","true");
    System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.75 Safari/535.7");

    this.options = prepareOptions();
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    String[] pios = cmd.getOptionValues("pio");
    String[] pioExpansions = cmd.getOptionValues("pio-expansions");
    String[] yamls = cmd.getOptionValues("yaml");
    if((pios==null)&&(yamls==null)){
      printUsage();
      return;
    }
    boolean exportYaml = cmd.hasOption("export-yaml");
    boolean exportImplTmpl = cmd.hasOption("export-impl-tmpl");
    boolean downloadScans = cmd.hasOption("download-scans");
    boolean exportE3 = cmd.hasOption("export-e3");
    if(!exportImplTmpl&&!exportYaml&&!downloadScans&&!exportE3){
      log.warn("Nothing to do. Please specify an output option");
      printUsage();
      return;
    }
    List<Card> allCards=new ArrayList<>();
    List<Expansion> allExpansions=new ArrayList<>();
    readPios(pios, pioExpansions, allCards);
    readYamls(yamls, allCards, allExpansions);
    SetWriter setWriter = new SetWriter();
    List<Expansion> expansions = setWriter.prepareAndOrderExpansionFiles(allCards);
    if(exportE3){
      List<ExpansionFile3> expansionFile3s = setWriter.convertFromE2ToE3(expansions);
      expansionFile3s.sort(Comparator.comparing(expansionFile3 -> expansionFile3.getExpansion().getOrderId()));
      setWriter.prepareReprintsE3(expansionFile3s);
      doVariantDebugging(expansionFile3s);
      String outputDirectory = "output/e3";
      setWriter.writeAllE3(expansionFile3s, outputDirectory);
      log.info("E3 YAMLs have been written to {} folder. Please copy them under src/main/resources/cards if you want them to take over.", outputDirectory);
    }
    setWriter.prepareReprints(expansions);
//		setWriter.fixGymSeriesEvolvesFromIssue(setFileMap.values());
    if(downloadScans){
      ScanDownloader scanDownloader = new ScanDownloader();
      scanDownloader.downloadAll(allCards);
      log.info("Scans have been saved into ./scans folder. Please upload them to scans server.");
    }
    if(exportYaml){
      setWriter.writeAllE2(expansions, "output");
      log.info("YAMLs have been written to ./output folder. Please copy them under src/main/resources/cards if you want them to take over.");
    }
    if(exportImplTmpl){
      ImplTmplGenerator implTmplGenerator = new ImplTmplGenerator();
      implTmplGenerator.writeAll(expansions);
      log.info("Implementation Templates (Groovy files) have been written to ./impl folder. Please copy them under contrib repo.");
    }
  }

  private static void doVariantDebugging(List<ExpansionFile3> all) {
    cardStreamOfExpansion(all, "LC").filter(c -> StringUtils.isBlank(c.getVariantOf())).forEach(c -> {
      String adversaryText = c.generateDiscriminatorFullText();
      log.debug(">>>MISSING VARIANT: {}", adversaryText);
      cardStreamOfExpansion(all, "BS").filter(c1 -> c1.getName().equals(c.getName())).forEach(c1 -> {
        String baseText = c1.generateDiscriminatorFullText();
        log.debug("\t\tpossible variant: {}", baseText);
        log.debug("\t\tDIFF: {}", StringUtils.difference(baseText, adversaryText));
      });
    });
  }

  private static Stream<Card3> cardStreamOfExpansion(List<ExpansionFile3> all, String expansionShortName) {
    return all.stream().filter(ef3 -> ef3.getExpansion().getShortName().equals(expansionShortName)).flatMap(ef3 -> ef3.getCards().stream());
  }

  private Options prepareOptions() {
    Options options = new Options();
    options.addOption(null, "pio", true, "Load pokemontcg.io (https://github.com/PokemonTCG/pokemon-tcg-data/tree/master/cards/en) or kirby's (https://github.com/kirbyUK/ptcgo-data/tree/master/en_US) files. e.g. '--pio=Unbroken Bonds.json' '--pio=Detective Pikachu.json' '--pio=../sm9.json' '--pio=../det1.json' and so on. Multiple files can be loaded this way.");
    options.addOption(null, "pio-expansions", true, "pokemontcg.io expansions file (https://github.com/PokemonTCG/pokemon-tcg-data/blob/master/sets/en.json)");
    options.addOption(null, "yaml", true, "TCG ONE carddb yaml files. e.g. '--yaml=423-unbroken_bonds.yaml' and so on. Multiple files can be loaded this way.");
    options.addOption(null, "export-yaml", false, "GOAL: export TCG ONE carddb yaml files");
    options.addOption(null, "export-implementations", false, "GOAL: export TCG ONE engine implementation template files");
    options.addOption(null, "download-scans", false, "GOAL: download scans");
    options.addOption(null, "export-e3", false, "GOAL: upgrade from TCG ONE carddb e2 schema to e3 schema then export them");
    return options;
  }

  private void readPios(String[] pios, String[] pioExpansions, List<Card> allCards) throws IOException {
    if(pios !=null){
      ArrayList<String> expansionIds = new ArrayList<>();

      for (String filename : pios) {
        expansionIds.add(Paths.get(filename).getFileName().toString().split("\\.")[0]);
      }

      if (pioExpansions != null) {
        for (String filename : pioExpansions) {
          log.info("Reading {}", filename);
          pioReader.loadExpansions(Files.newInputStream(Paths.get(filename)), expansionIds);
        }
      }

      for (String filename : pios) {
        log.info("Reading {}", filename);
        allCards.addAll(pioReader.load(Files.newInputStream(Paths.get(filename))));
      }
    }
  }

  private void readYamls(String[] yamls, List<Card> allCards, List<Expansion> allExpansions) throws IOException {
    if(yamls !=null){
      for (String filename : yamls) {
        Stack<File> fileStack = new Stack<>();
        File file = new File(filename);
        if(file.isDirectory()){
          for (File file1 : Objects.requireNonNull(file.listFiles())) {
            fileStack.push(file1);
          }
        } else {
          fileStack.push(file);
        }
        while (!fileStack.isEmpty()){
          File currentFile = fileStack.pop();
          if(!currentFile.getName().endsWith("yaml")) continue;
          log.info("Reading {}", currentFile.getName());
          Expansion expansion = readExpansion(currentFile);
          allExpansions.add(expansion);
          allCards.addAll(expansion.getCards());
        }
      }
    }
  }

  private Expansion readExpansion(File pop) throws IOException {
    Expansion expansion = mapper.readValue(Files.newInputStream(pop.toPath()), Expansion.class);
    for (Card card : expansion.getCards()) {
      card.setExpansion(expansion); // temporary
    }
    return expansion;
  }

  private void printUsage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("TCG ONE carddb tools loads and converts Pokemon TCG card data from various formats into TCG ONE Card Database format and/or TCG ONE Engine Implementation Templates", options);
  }
}
