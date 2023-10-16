package tcgone.carddb.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.cli.*;
import tcgone.carddb.model.Card;
import tcgone.carddb.model.Expansion;
import tcgone.carddb.model3.ExpansionFile3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

/**
 * @author axpendix@hotmail.com
 */
public class Application {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Application.class);
  private Options options;

  public static void main(String[] args) throws Exception {
    System.setProperty("java.net.useSystemProxies","true");
    System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.75 Safari/535.7");
    new Application(args);
  }

  private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
  private final PioReader pioReader=new PioReader();
  private final SetWriter setWriter=new SetWriter();
  private final ScanDownloader scanDownloader=new ScanDownloader();
  private final ImplTmplGenerator implTmplGenerator=new ImplTmplGenerator();

  public Application(String[] args) throws Exception {

    Options options = prepareOptions();
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
    Collection<Expansion> expansions = setWriter.prepareSetFiles(allCards);
    setWriter.prepareReprints(expansions);
//		setWriter.fixGymSeriesEvolvesFromIssue(setFileMap.values());
    if(exportE3){
      List<ExpansionFile3> expansionFile3s = setWriter.convertFromE2ToE3(expansions);
      setWriter.writeAllE3(expansionFile3s, "output3");
      log.info("E3 YAMLs have been written to ./output folder. Please copy them under src/main/resources/cards if you want them to take over.");
    }
    if(downloadScans){
      scanDownloader.downloadAll(allCards);
      log.info("Scans have been saved into ./scans folder. Please upload them to scans server.");
    }
    if(exportYaml){
      setWriter.writeAllE2(expansions, "output");
      log.info("YAMLs have been written to ./output folder. Please copy them under src/main/resources/cards if you want them to take over.");
    }
    if(exportImplTmpl){
      implTmplGenerator.writeAll(expansions);
      log.info("Implementation Templates (Groovy files) have been written to ./impl folder. Please copy them under contrib repo.");
    }
  }

  private static Options prepareOptions() {
    Options options = new Options();
    options.addOption("pio", true, "pokemontcg.io input files");
    options.addOption("pio-expansions", true, "pokemontcg.io expansions file");
    options.addOption("yaml", true, "tcgone carddb yaml files");
    options.addOption("export-yaml", "export tcgone carddb yaml files");
    options.addOption("export-impl-tmpl", "export tcgone engine implementation template files");
    options.addOption("download-scans", "download scans");
    options.addOption("export-e3", "upgrade from tcgone carddb e2 schema to e3 schema then export them");
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
          pioReader.loadExpansions(new FileInputStream(filename), expansionIds);
        }
      }

      for (String filename : pios) {
        log.info("Reading {}", filename);
        allCards.addAll(pioReader.load(new FileInputStream(filename)));
      }
    }
  }

  private void readYamls(String[] yamls, List<Card> allCards, List<Expansion> allExpansions) throws IOException {
    if(yamls !=null){
      for (String filename : yamls) {
        Stack<File> fileStack = new Stack<>();
        File file = new File(filename);
        if(file.isDirectory()){
          for (File file1 : file.listFiles()) {
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
    Expansion expansion = mapper.readValue(new FileInputStream(pop), Expansion.class);
    for (Card card : expansion.getCards()) {
      card.setExpansion(expansion); // temporary
    }
    return expansion;
  }

  private void printUsage() {
    System.out.println("This tool loads and converts pio format Pokemon TCG data into TCG ONE Card Database format and/or TCG ONE Card Implementation Groovy Template. \n" +
      "Load pio files (https://github.com/PokemonTCG/pokemon-tcg-data/tree/master/json/cards) or kirby files (https://github.com/kirbyUK/ptcgo-data/tree/master/en_US) by; \n" +
      "\t'--pio=Unbroken Bonds.json' '--pio=Detective Pikachu.json' '--pio=../sm9.json' '--pio=../det1.json' and so on. Multiple files can be loaded this way.\n" +
      "and/or load TCG ONE yaml files directly by; \n" +
      "\t'--yaml=423-unbroken_bonds.yaml' and so on. Multiple files can be loaded this way.\n" +
      "then, export to yaml or impl-tmpl;\n" +
      "\t--export-yaml --export-impl-tmpl\n" +
      "and/or download scans;\n" +
      "\t--download-scans");

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("ant", options);
  }
}
