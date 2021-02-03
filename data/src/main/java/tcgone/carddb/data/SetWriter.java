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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.util.NodeStyleResolver;
import com.fasterxml.jackson.dataformat.yaml.util.StringQuotingChecker;
import gnu.trove.set.hash.THashSet;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.*;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.util.PlatformFeatureDetector;
import tcgone.carddb.model.Set;
import tcgone.carddb.model.*;
import tcgone.carddb.model.experimental.VariantType;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.*;
import static org.yaml.snakeyaml.introspector.BeanAccess.DEFAULT;
import static org.yaml.snakeyaml.introspector.BeanAccess.FIELD;


/**
 * @author axpendix@hotmail.com
 */
public class SetWriter {
  private final YAMLMapper mapper;
  //  public static class CustomPropertyUtils extends PropertyUtils {
//
//    private final Map<Class<?>, Map<String, Property>> propertiesCache = new HashMap<Class<?>, Map<String, Property>>();
//    private final Map<Class<?>, java.util.Set<Property>> readableProperties = new HashMap<Class<?>, java.util.Set<Property>>();
//    private BeanAccess beanAccess = DEFAULT;
//    private boolean allowReadOnlyProperties = false;
//    private boolean skipMissingProperties = false;
//
//    private PlatformFeatureDetector platformFeatureDetector;
//
//    public CustomPropertyUtils() {
//      this(new PlatformFeatureDetector());
//    }
//
//    CustomPropertyUtils(PlatformFeatureDetector platformFeatureDetector) {
//      this.platformFeatureDetector = platformFeatureDetector;
//
//      /*
//       * Android lacks much of java.beans (including the Introspector class, used here), because java.beans classes tend to rely on java.awt, which isn't
//       * supported in the Android SDK. That means we have to fall back on FIELD access only when SnakeYAML is running on the Android Runtime.
//       */
//      if (platformFeatureDetector.isRunningOnAndroid()) {
//        beanAccess = FIELD;
//      }
//    }
//
//    protected Map<String, Property> getPropertiesMap(Class<?> type, BeanAccess bAccess) {
//      if (propertiesCache.containsKey(type)) {
//        return propertiesCache.get(type);
//      }
//
//      Map<String, Property> properties = new LinkedHashMap<String, Property>();
//      boolean inaccessableFieldsExist = false;
//      switch (bAccess) {
//        case FIELD:
//          for (Class<?> c = type; c != null; c = c.getSuperclass()) {
//            for (Field field : c.getDeclaredFields()) {
//              int modifiers = field.getModifiers();
//              if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)
//                && !properties.containsKey(field.getName())) {
//                properties.put(field.getName(), new FieldProperty(field));
//              }
//            }
//          }
//          break;
//        default:
//          // add JavaBean properties
//          try {
//            for (PropertyDescriptor property : Introspector.getBeanInfo(type)
//              .getPropertyDescriptors()) {
//              Method readMethod = property.getReadMethod();
//              if ((readMethod == null || !readMethod.getName().equals("getClass"))
//                && !isTransient(property)) {
//                properties.put(property.getName(), new MethodProperty(property));
//              }
//            }
//          } catch (IntrospectionException e) {
//            throw new YAMLException(e);
//          }
//
//          // add public fields
//          for (Class<?> c = type; c != null; c = c.getSuperclass()) {
//            for (Field field : c.getDeclaredFields()) {
//              int modifiers = field.getModifiers();
//              if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
//                if (Modifier.isPublic(modifiers)) {
//                  properties.put(field.getName(), new FieldProperty(field));
//                } else {
//                  inaccessableFieldsExist = true;
//                }
//              }
//            }
//          }
//          break;
//      }
//      if (properties.isEmpty() && inaccessableFieldsExist) {
//        throw new YAMLException("No JavaBean properties found in " + type.getName());
//      }
//      System.out.println(properties);
//      propertiesCache.put(type, properties);
//      return properties;
//    }
//
//    private static final String TRANSIENT = "transient";
//
//    private boolean isTransient(FeatureDescriptor fd) {
//      return Boolean.TRUE.equals(fd.getValue(TRANSIENT));
//    }
//
//    public java.util.Set<Property> getProperties(Class<? extends Object> type) {
//      return getProperties(type, beanAccess);
//    }
//
//    public java.util.Set<Property> getProperties(Class<? extends Object> type, BeanAccess bAccess) {
//      if (readableProperties.containsKey(type)) {
//        return readableProperties.get(type);
//      }
//      java.util.Set<Property> properties = createPropertySet(type, bAccess);
//      readableProperties.put(type, properties);
//      return properties;
//    }
//
//    protected java.util.Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess) {
//      java.util.Set<Property> properties = new LinkedHashSet<>();
//      Collection<Property> props = getPropertiesMap(type, bAccess).values();
//      for (Property property : props) {
//        if (property.isReadable() && (allowReadOnlyProperties || property.isWritable())) {
//          properties.add(property);
//        }
//      }
//      return properties;
//    }
//
//    public Property getProperty(Class<? extends Object> type, String name) {
//      return getProperty(type, name, beanAccess);
//    }
//
//    public Property getProperty(Class<? extends Object> type, String name, BeanAccess bAccess) {
//      Map<String, Property> properties = getPropertiesMap(type, bAccess);
//      Property property = properties.get(name);
//      if (property == null && skipMissingProperties) {
//        property = new MissingProperty(name);
//      }
//      if (property == null) {
//        throw new YAMLException(
//          "Unable to find property '" + name + "' on class: " + type.getName());
//      }
//      return property;
//    }
//
//    public void setBeanAccess(BeanAccess beanAccess) {
//      if (platformFeatureDetector.isRunningOnAndroid() && beanAccess != FIELD) {
//        throw new IllegalArgumentException(
//          "JVM is Android - only BeanAccess.FIELD is available");
//      }
//
//      if (this.beanAccess != beanAccess) {
//        this.beanAccess = beanAccess;
//        propertiesCache.clear();
//        readableProperties.clear();
//      }
//    }
//
//    public void setAllowReadOnlyProperties(boolean allowReadOnlyProperties) {
//      if (this.allowReadOnlyProperties != allowReadOnlyProperties) {
//        this.allowReadOnlyProperties = allowReadOnlyProperties;
//        readableProperties.clear();
//      }
//    }
//
//    public boolean isAllowReadOnlyProperties() {
//      return allowReadOnlyProperties;
//    }
//
//    /**
//     * Skip properties that are missing during deserialization of YAML to a Java
//     * object. The default is false.
//     *
//     * @param skipMissingProperties true if missing properties should be skipped, false otherwise.
//     */
//    public void setSkipMissingProperties(boolean skipMissingProperties) {
//      if (this.skipMissingProperties != skipMissingProperties) {
//        this.skipMissingProperties = skipMissingProperties;
//        readableProperties.clear();
//      }
//    }
//
//    public boolean isSkipMissingProperties() {
//      return skipMissingProperties;
//    }
//  }

  public static class EqualityCard extends Card {

    public EqualityCard(Card card) {
      this.name = card.name;
      this.types = card.types;
      this.superType = card.superType;
      this.subTypes = card.subTypes;
      this.evolvesFrom = card.evolvesFrom;
      this.hp = card.hp;
      this.retreatCost = card.retreatCost;
      this.abilities = card.abilities;
      this.moves = card.moves;
      this.weaknesses = card.weaknesses;
      this.resistances = card.resistances;
      this.text = card.text;
      this.energy = card.energy;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      EqualityCard card = (EqualityCard) o;
      return Objects.equals(name, card.name) &&
        Objects.equals(types, card.types) &&
        Objects.equals(superType, card.superType) &&
        Objects.equals(subTypes, card.subTypes) &&
        Objects.equals(evolvesFrom, card.evolvesFrom) &&
        Objects.equals(hp, card.hp) &&
        Objects.equals(retreatCost, card.retreatCost) &&
        Objects.equals(abilities, card.abilities) &&
        Objects.equals(moves, card.moves) &&
        Objects.equals(weaknesses, card.weaknesses) &&
        Objects.equals(resistances, card.resistances) &&
        Objects.equals(text, card.text) &&
        Objects.equals(energy, card.energy);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, types, superType, subTypes, evolvesFrom, hp, retreatCost, abilities, moves, weaknesses, resistances, text, energy);
    }
  }

//  private Yaml yaml;
//  private ObjectMapper objectMapper;

  SetWriter() {
//    DumperOptions options = new DumperOptions();
//    options.setAllowUnicode(true);
//    options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
//    options.setDefaultFlowStyle(DumperOptions.FlowStyle.AUTO);
//
//    CustomPropertyUtils customPropertyUtils = new CustomPropertyUtils();
//    Representer customRepresenter = new Representer() {
//      @Override
//      protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
//        // if value of property is null, ignore it.
//        if (propertyValue == null) {
//          return null;
//        } else {
//          return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
//        }
//      }
//
//      protected MappingNode representJavaBean(java.util.Set<Property> properties, Object javaBean) {
//        List<NodeTuple> value = new ArrayList<NodeTuple>(properties.size());
//        Tag tag;
//        Tag customTag = classTags.get(javaBean.getClass());
//        tag = customTag != null ? customTag : new Tag(javaBean.getClass());
//        // flow style will be chosen by BaseRepresenter
//        MappingNode node = new MappingNode(tag, value, DumperOptions.FlowStyle.AUTO);
//        representedObjects.put(javaBean, node);
//        DumperOptions.FlowStyle bestStyle = DumperOptions.FlowStyle.BLOCK;
//        for (Property property : properties) {
//          Object memberValue = property.get(javaBean);
//          Tag customPropertyTag = memberValue == null ? null
//            : classTags.get(memberValue.getClass());
//          NodeTuple tuple = representJavaBeanProperty(javaBean, property, memberValue,
//            customPropertyTag);
//          if (tuple == null) {
//            continue;
//          }
//          if (!((ScalarNode) tuple.getKeyNode()).isPlain()) {
//            bestStyle = DumperOptions.FlowStyle.BLOCK;
//          }
//          Node nodeValue = tuple.getValueNode();
//          if (!(nodeValue instanceof ScalarNode && ((ScalarNode) nodeValue).isPlain())) {
//            bestStyle = DumperOptions.FlowStyle.BLOCK;
//          }
//          if ("abilities".equals(((ScalarNode) tuple.getKeyNode()).getValue())) {
//            bestStyle = DumperOptions.FlowStyle.BLOCK;
//          }
//          value.add(tuple);
//        }
//        if (defaultFlowStyle != DumperOptions.FlowStyle.AUTO) {
//          node.setFlowStyle(defaultFlowStyle);
//        } else {
//          node.setFlowStyle(bestStyle);
//        }
//        return node;
//      }
//
//      @Override
//      protected Node representMapping(Tag tag, Map<?, ?> mapping, DumperOptions.FlowStyle flowStyle) {
//        List<NodeTuple> value = new ArrayList<NodeTuple>(mapping.size());
//        MappingNode node = new MappingNode(tag, value, flowStyle);
//        representedObjects.put(objectToRepresent, node);
//        DumperOptions.FlowStyle bestStyle = DumperOptions.FlowStyle.FLOW;
//        for (Map.Entry<?, ?> entry : mapping.entrySet()) {
//          Node nodeKey = representData(entry.getKey());
//          Node nodeValue = representData(entry.getValue());
//          if (!(nodeKey instanceof ScalarNode && ((ScalarNode) nodeKey).isPlain())) {
//            bestStyle = DumperOptions.FlowStyle.BLOCK;
//          }
//          if (!(nodeValue instanceof ScalarNode && ((ScalarNode) nodeValue).isPlain())) {
//            bestStyle = DumperOptions.FlowStyle.BLOCK;
//          }
//          value.add(new NodeTuple(nodeKey, nodeValue));
//        }
//        if (flowStyle == DumperOptions.FlowStyle.AUTO) {
//          if (defaultFlowStyle != DumperOptions.FlowStyle.AUTO) {
//            node.setFlowStyle(defaultFlowStyle);
//          } else {
//            node.setFlowStyle(bestStyle);
//          }
//        }
//        return node;
//      }
//    };
//    customRepresenter.setPropertyUtils(customPropertyUtils);
//    yaml = new Yaml(customRepresenter, options);


    mapper = YAMLMapper.builder(
      YAMLFactory.builder()
        .nodeStyleResolver(s -> ("cost".equals(s)||"types".equals(s)||"subTypes".equals(s)||"evolvesTo".equals(s)) ? NodeStyleResolver.NodeStyle.FLOW : null)
        .stringQuotingChecker(new StringQuotingChecker.Default() {
//          THashSet<String> a=new THashSet<>();
//          {
//            a.add("id");
//            a.add("number");
//          }
//          @Override
//          public boolean needToQuoteName(String s) {
//            return a.contains(s);
//          }

          @Override
          public boolean needToQuoteValue(String s) {
            // https://yaml.org/spec/1.1/#plain%20style/syntax
            if (s != null && !s.contains(": ") && !s.contains(" #") && !s.contains("\t")
              && !s.startsWith("- ") && !s.startsWith("? ") && !s.startsWith(":"))
              return false;
            else
              return super.needToQuoteValue(s);
          }
        })
        .build()
      )
      .enable(ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
      .enable(MINIMIZE_QUOTES)
//      .enable(LITERAL_BLOCK_STYLE)
      .disable(WRITE_DOC_START_MARKER)
      .nodeFactory(new SortingNodeFactory())
      .build();
//    objectMapper = new ObjectMapper(new YAMLFactory()
//      .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
//    );


  }
  static class SortingNodeFactory extends JsonNodeFactory {
    @Override
    public ObjectNode objectNode() {
      return new ObjectNode(this, new TreeMap<String, JsonNode>());
    }
  }
  public void writeAll(Collection<Set> sets,String outputDirectory) throws IOException {
    new File(outputDirectory).mkdirs();
    for (Set set : sets) {
      set.filename = String.format(outputDirectory+File.separator+"%s-%s.yaml", set.id, set.enumId.toLowerCase(Locale.ENGLISH));
      for (Card card : set.cards) {
        card.set = null;
        card.merged = null;
        card.formats = null;
        if (card.moves != null) {
          for (Move move : card.moves) {
            if (move.damage != null && move.damage.isEmpty()) {
              move.damage = null;
            }
            if (move.text != null && move.text.isEmpty()) {
              move.text = null;
            }
  //          if (move.cost != null && move.cost.size() == 1 && move.cost.get(0) == null) {
  //            move.cost = new ArrayList<>();
  //          }
          }
        }
      }
      BufferedWriter out = new BufferedWriter
        (new OutputStreamWriter(new FileOutputStream(set.filename), StandardCharsets.UTF_8));
      set.filename=null;
      mapper.writeValue(out,set);
//      String dump = yaml.dumpAs(set, Tag.MAP, null);
//      out.write(dump);
      out.close();
    }
  }

  public Collection<Set> prepareSetFiles(List<Card> cards) {
    Map<String, Set> expansionMap = new HashMap<>();
    for (Card card : cards) {
      String key = card.set.enumId;
      if (!expansionMap.containsKey(key)) {
        Set set = new Set();
        card.set.copyStaticPropertiesTo(set);
        set.cards = new ArrayList<>();
        expansionMap.put(key, set);
      }
      expansionMap.get(key).cards.add(card);
    }
    for (Set set : expansionMap.values()) {
      set.cards.sort((o1, o2) -> {
        try {
          Integer n1 = Integer.parseInt(o1.number);
          Integer n2 = Integer.parseInt(o2.number);
          return n1.compareTo(n2);
        } catch (NumberFormatException e) {
          return o1.number.compareTo(o2.number);
        }
      });
    }
    return expansionMap.values();
  }

  public void prepareReprints(Collection<Set> setFiles) {
    Map<EqualityCard, Card> map = new HashMap<>();
    for (Set setFile : setFiles) {
      for (Card c : setFile.cards) {
//                int hash = Objects.hash(c.name, c.types, c.superType, c.subTypes, c.evolvesFrom, c.hp, c.retreatCost, c.abilities, c.moves, c.weaknesses, c.resistances, c.text, c.energy);
        EqualityCard ec = new EqualityCard(c);
        if (map.containsKey(ec)) {
          Card oc = map.get(ec);
          if (c.rarity == Rarity.ULTRA_RARE) {
            // most likely full art
            c.variantType = VariantType.FULL_ART;
          } else if (c.rarity == Rarity.SECRET) {
            // most likely secret art
            c.variantType = VariantType.SECRET_ART;
          } else {
            c.variantType = VariantType.REPRINT;
          }
          c.variantOf = oc.id;
        } else {
          map.put(ec, c);
        }
      }
    }
  }

  public void fixGymSeriesEvolvesFromIssue(Collection<SetFile> setFiles) {
    List<String> owners = Arrays.asList("Blaine's", "Brock's", "Misty's", "Lt. Surge's", "Sabrina's", "Erika's", "Koga's", "Giovanni's");
    for (SetFile setFile : setFiles) {
      if(setFile.set.name.contains("Gym ")){
        for (Card card : setFile.cards) {
          if(card.subTypes.contains(CardType.EVOLUTION)){
            for (String owner : owners) {
              if(card.name.startsWith(owner)){
                if(card.evolvesFrom == null){
                  System.out.println("NoEvolvesFrom:"+card.name);
                }
                if(!card.evolvesFrom.startsWith(owner)){
                  System.out.println(card.name);
                  card.evolvesFrom = owner + " " + card.evolvesFrom;
                  break;
                }
              }
            }
          }
        }
      }
    }
  }
}