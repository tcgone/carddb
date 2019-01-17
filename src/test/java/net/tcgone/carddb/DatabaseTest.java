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
package net.tcgone.carddb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.tcgone.carddb.model.Card;
import net.tcgone.carddb.model.Format;
import net.tcgone.carddb.model.SetFile;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.Assert.assertTrue;

/**
 * @author axpendix@hotmail.com
 * @since 31.12.2018
 */
public class DatabaseTest {

	private static final Logger log = LoggerFactory.getLogger(DatabaseTest.class);
	private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	private final PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
	private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final Validator validator = factory.getValidator();

	@Test
	public void testCards() throws IOException {
		Resource[] resources = resourceResolver.getResources("classpath:/cards/*.yaml");
		for (Resource resource : resources) {
			SetFile setFile = mapper.readValue(resource.getInputStream(), SetFile.class);
			setFile.filename = resource.getFilename();
			validateSetFile(setFile);
			log.info("Validated {}", setFile.set.name);
		}
	}

	private void validateAndAssert(String context, Object o){
		Set<ConstraintViolation<Object>> violations = validator.validate(o);
		String message = violations.stream()
				.map(cv -> String.format("%s %s %s", context, cv.getPropertyPath().toString(), cv.getMessage()))
				.collect(Collectors.joining(","));
		assertTrue(message, violations.isEmpty());
	}

	@Test
	public void testFormats() throws IOException {
		List<Format> formats = mapper.readValue(resourceResolver.getResource("classpath:/formats.yaml").getInputStream(), new TypeReference<List<Format>>(){});
		for (Format format : formats) {
			validateAndAssert(format.enumId, format);
		}
	}

	private void validateSetFile(SetFile setFile){
		validateAndAssert(setFile.filename, setFile);
		if(setFile.set == null || setFile.set.abbr == null){
			throw new IllegalArgumentException("set is empty: "+setFile.filename);
		}
		//|| isBlank(setFile.set.pioId) || isBlank(setFile.set.seoName)
		if(isBlank(setFile.set.abbr) || isBlank(setFile.set.enumId) || isBlank(setFile.set.name) || isBlank(setFile.set.id) ){
			throw new IllegalArgumentException("a property of set is empty: "+setFile.filename);
		}
//        if(setFile.cards.isEmpty()){
//            throw new IllegalArgumentException("no cards in: "+setFile.filename);
//        }
		for (Card card : setFile.cards) {
			validateCard(card, setFile);
		}
	}

	private static final List<String> validSuperTypes = Arrays.asList("Pokémon", "Trainer", "Energy");
	private static final Map<String, Set<String>> validSubTypes = ImmutableMap.<String, Set<String>>builder()
			.put("Pokémon", ImmutableSet.of("Basic", "Stage 1", "Stage 2", "Mega", "EX", "ex", "Break", "LV.X", "Restored", "Baby", "Prime", "GX"))
			.put("Trainer", ImmutableSet.of("Basic", "Supporter", "Stadium", "Item", "Pokémon Tool", "Technical Machine", "Flare", "Ace Spec", "Fossil"))
			.put("Energy", ImmutableSet.of("Basic", "Special")).build();

	private void validateCard(Card card, SetFile setFile){
		if(card == null){
			throw new IllegalArgumentException("card null in: "+setFile.filename);
			// TODO
		}
		// check super type
//        card.superType = WordUtils.capitalizeFully(card.superType);
//        if(card.superType.equals("Pokemon")){
//            card.superType = "Pokémon";
//        }
//        if(!validSuperTypes.contains(card.superType)){
//            throw new IllegalArgumentException(String.format("Invalid super type %s in %s", card.superType, setFile.filename));
//        }
		// check sub type
		// check empty/null fields, number, ordering, etc, attacks, abilities
	}

}
