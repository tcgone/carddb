package tcgone.carddb.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;


/**
 *     1. read everything then check version field. if at least one version is not up to date;
 *       1. call upgrade methods in order, until the final one, for every stale object
 *       2. call setwriter and save the object to src/main/resources/cards
 *       3. exit with error. developer will restart the process.
 *     2. read convert_pio_to_yaml/*.json
 *       1. convert all files into yaml by using setwriter struct. it can save with an aux method. it will call upgrade() for every output
 *       2. delete the input files
 *       3. exit with error. developer will restart the process.
 *     3. read convert_yaml_to_impl/*.yaml
 *       1. convert all files into implementation templates via impltmplgenerator.
 *       2. delete the input files
 *       3. exit with error. developer will move the output files into tcgone-engine repository.
 *     4. read download_scans/*
 *       1. download all scans of given files (which format?)
 *       2. delete the input files
 *       3. exit with error. developer will upload the output files onto image server.
 *
 */

public class Upgrader {

  private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
  private final PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
  private final SetWriter setWriter = new SetWriter();

  public Upgrader() throws Exception {
//    upgradeE1E2();
  }

//  void upgradeE1E2() throws IOException {
//    // read set files
//    Resource[] resources = resourceResolver.getResources("classpath:/cards/*.yaml");
//    List<Set> sets=new ArrayList<>();
//    for (Resource resource : resources) {
//      SetFile s1 = mapper.readValue(resource.getInputStream(), SetFile.class);
//      s1.set.filename=resource.getFilename();
//      s1.set.cards=s1.cards;
//      s1.set.schema="E2";//EXPANSIONS2
//      sets.add(s1.set);
//    }
//    //SAVENOW
//    if (sets.size()>0) {
//      setWriter.writeAll(sets,"output");
//      System.out.println("EXPORTED "+sets.size()+" SETS");
//    }
//  }


  public static void main(String[] args) throws Exception {
    new Upgrader();
  }
}
