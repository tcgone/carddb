package net.tcgone.carddb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResults {
  private List<CardLite> data;
  private Integer totalPages;
  private Integer totalElements;
  private Long responseTimeMillis;
  private String errorMessage;
}
