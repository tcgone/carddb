package tcgone.carddb.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import tcgone.carddb.data.Importer;
import tcgone.carddb.model.Card;
import tcgone.carddb.model.EnhancedCard;
import tcgone.carddb.model.ExpansionFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
  private List<ExpansionFile> expansionFiles = new ArrayList<>();

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
    if(!exportImplTmpl&&!exportYaml&&!downloadScans){
      log.warn("Nothing to do. Please specify an output option");
      printUsage();
      return;
    }
    if (pios != null) {
      readPios(pios, pioExpansions);
    }
    if (yamls !=null) {
      expansionFiles = readYamlsCrude(yamls);
//      Importer importer = importYamls(yamls);
//      expansionFiles.addAll(importer.getExpansionFiles());
    }
    if (expansionFiles.isEmpty()) {
      log.error("No expansion files could be found!");
    }
    SetWriter setWriter = new SetWriter();
    expansionFiles.sort(Comparator.comparing(expansionFile -> expansionFile.getExpansion().getOrderId()));
//		setWriter.fixGymSeriesEvolvesFromIssue(setFileMap.values());
    if(downloadScans){
      ScanDownloader scanDownloader = new ScanDownloader();
      scanDownloader.downloadAll(expansionFiles);
      log.info("Scans have been saved into ./scans folder. Please upload them to scans server.");
    }
    if(exportYaml){
      setWriter.detectAndSetReprints(expansionFiles);
      doVariantDebugging(expansionFiles);
      String outputDirectory = "output/e3";
      setWriter.writeAllE3(expansionFiles, outputDirectory);
      log.info("E3 YAMLs have been written to {} folder. Please copy them under src/main/resources/cards if you want them to take over.", outputDirectory);
    }
    if(exportImplTmpl){
      ImplTmplGenerator implTmplGenerator = new ImplTmplGenerator();
      implTmplGenerator.writeAll(expansionFiles);
      log.info("Implementation Templates (Groovy files) have been written to ./impl folder. Please copy them under contrib repo.");
    }
  }

  private static void doVariantDebugging(List<ExpansionFile> all) {
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

  private static Stream<Card> cardStreamOfExpansion(List<ExpansionFile> all, String expansionShortName) {
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
    return options;
  }

  private void readPios(String[] pios, String[] pioExpansions) throws IOException {
    if (pioExpansions != null) {
      pioReader.loadExpansions(pioExpansions);
    }
    Stack<File> fileStack = prepareFileStack(pios);
    List<ExpansionFile> loaded = pioReader.load(fileStack);
    expansionFiles.addAll(loaded);
  }

  private static List<EnhancedCard> addAndEnhanceAllCardsFromExpansionFiles(List<ExpansionFile> loaded) {
    List<EnhancedCard> allCards = new ArrayList<>();
    for (ExpansionFile expansionFile : loaded) {
      for (Card card : expansionFile.getCards()) {
        EnhancedCard enhancedCard = EnhancedCard.fromCard(card);
        enhancedCard.setExpansion(expansionFile.getExpansion());
        allCards.add(enhancedCard);
      }
    }
    return allCards;
  }

  private List<ExpansionFile> readYamlsCrude(String[] yamls) throws IOException {
    List<ExpansionFile> list = new ArrayList<>();
    Stack<File> fileStack = prepareFileStack(yamls);
    while (!fileStack.isEmpty()){
      File currentFile = fileStack.pop();
      if(!currentFile.getName().endsWith("yaml")) continue;
      log.info("Reading {}", currentFile.getName());
      list.add(readExpansionFile(currentFile));
    }
    return list;
  }

  private Importer importYamls(String[] yamls) {
    Stack<File> fileStack = prepareFileStack(yamls);
    return new Importer(fileStack);
  }

  private static Stack<File> prepareFileStack(String[] inputFileOrFolderPaths) {
    Stack<File> fileStack = new Stack<>();
    for (String filename : inputFileOrFolderPaths) {
      File file = new File(filename);
      if(file.isDirectory()){
        for (File file1 : Objects.requireNonNull(file.listFiles())) {
          fileStack.push(file1);
        }
      } else {
        fileStack.push(file);
      }
    }
    return fileStack;
  }

  private ExpansionFile readExpansionFile(File pop) throws IOException {
    return mapper.readValue(Files.newInputStream(pop.toPath()), ExpansionFile.class);
  }

  private void printUsage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("TCG ONE carddb tools loads and converts Pokemon TCG card data from various formats into TCG ONE Card Database format and/or TCG ONE Engine Implementation Templates", options);
  }
}
