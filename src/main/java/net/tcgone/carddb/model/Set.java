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
package net.tcgone.carddb.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

import static net.tcgone.carddb.model.Card.ID_PATTERN;

public class Set {
  @NotBlank
  @Pattern(regexp = ID_PATTERN)
  public String id;
  @NotBlank
  public String name;
  public String pioId;
  public String seoName;
  @NotBlank
  public String enumId;
  @NotBlank
  public String abbr;
  // respective to all sets
  public Integer order;
  public List<String> categories;
  public Integer officialCount;
  public String imageUrl;
  public String symbolUrl;
  /**
   * If the entire set is not implemented yet, put this flag up
   */
  public boolean notImplemented;
  /**
   * all cards of this set, populated at runtime
   */
  @JsonIgnore
  public transient List<Card> _cards;
  /**
   * all formats that this set is allowed in, including partial sets, populated at runtime
   */
  @JsonIgnore
  public transient List<Format> _formats;

  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
