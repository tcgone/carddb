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
package net.tcgone.carddb.test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.tcgone.carddb.Importer;
import net.tcgone.carddb.model.Card;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;

/**
 * @author axpendix@hotmail.com
 */
public class ImportTest {

  private Importer importer;
  @Before
  public void testImport() throws Exception {
    importer = new Importer();
    importer.init();
  }
  @Test
  public void printNonIntegerNumberedCards() {
    Multimap<String, String> mmap = ArrayListMultimap.create();
    for (Card card : importer.getAllCards()) {
      if(!NumberUtils.isDigits(card.number)){
        mmap.put(card.set.enumId, card.enumId);
      }
    }
    for (Map.Entry<String, Collection<String>> entry : mmap.asMap().entrySet()) {
      System.out.println(entry.getKey() + ":" + entry.getValue());
    }
  }
}
