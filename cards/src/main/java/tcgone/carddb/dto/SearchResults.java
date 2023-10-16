package tcgone.carddb.dto;

import java.util.List;

public class SearchResults {
  private List<CardLite> data;
  private Integer totalPages;
  private Integer totalElements;
  private Long responseTimeMillis;
  private String errorMessage;

  public SearchResults() {
  }

  public SearchResults(List<CardLite> data, Integer totalPages, Integer totalElements, Long responseTimeMillis, String errorMessage) {
    this.data = data;
    this.totalPages = totalPages;
    this.totalElements = totalElements;
    this.responseTimeMillis = responseTimeMillis;
    this.errorMessage = errorMessage;
  }

  public List<CardLite> getData() {
    return data;
  }

  public SearchResults setData(List<CardLite> data) {
    this.data = data;
    return this;
  }

  public Integer getTotalPages() {
    return totalPages;
  }

  public SearchResults setTotalPages(Integer totalPages) {
    this.totalPages = totalPages;
    return this;
  }

  public Integer getTotalElements() {
    return totalElements;
  }

  public SearchResults setTotalElements(Integer totalElements) {
    this.totalElements = totalElements;
    return this;
  }

  public Long getResponseTimeMillis() {
    return responseTimeMillis;
  }

  public SearchResults setResponseTimeMillis(Long responseTimeMillis) {
    this.responseTimeMillis = responseTimeMillis;
    return this;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public SearchResults setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  @Override
  public String toString() {
    return "SearchResults{" +
      "data=" + data +
      ", totalPages=" + totalPages +
      ", totalElements=" + totalElements +
      ", responseTimeMillis=" + responseTimeMillis +
      ", errorMessage='" + errorMessage + '\'' +
      '}';
  }
}
