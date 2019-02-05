package net.tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class DTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class CardLite {
        private String id;
        private String name;
        private String imageUrl;
        private Integer quantity; //for inventory & deck
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class SearchResults {
        private List<CardLite> data;
        private Integer totalPages;
        private Integer totalElements;
        private String responseTime;
    }
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class SetLite {
        private String id;
        private String name;
        private String abbr;
    }
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class FormatLite {
        private String id;
        private String name;
        private String description;
    }

}
