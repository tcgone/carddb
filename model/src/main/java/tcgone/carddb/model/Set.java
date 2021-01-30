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
package tcgone.carddb.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

import static tcgone.carddb.model.Card.ID_PATTERN;

public class Set {
  /**
   * New, three digit id. This is immutable.
   */
  @NotBlank
  @Pattern(regexp = ID_PATTERN)
  public String id;
  /**
   * Full Set Name (e.g. Base Set)
   */
  @NotBlank
  public String name;
  /**
   * url compatible id (e.g. base-set)
   */
  public String seoName;
  /**
   * Enum id (core id) is used by game engine and card implementations
   */
  @NotBlank
  public String enumId;
  /**
   * Abbreviation. i.e. ptcgo code
   */
  @NotBlank
  public String abbr;
  public String pioId;

  // respective to all sets
  public Integer order;
  public List<String> categories;
  public String series;
  public Integer officialCount;
  public String releaseDate;
  public String imageUrl;
  public String symbolUrl;
  /**
   * If the entire set is not implemented yet, put this flag up
   */
  public boolean notImplemented;
  /**
   * all cards of this set, populated at runtime
   */
  public List<Card> cards;
  /**
   * all formats that this set is allowed in, including partial sets, populated at runtime
   */
  public List<Format> formats;

  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
