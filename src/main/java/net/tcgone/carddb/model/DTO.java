package net.tcgone.carddb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public class DTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data @AllArgsConstructor
    public static class CardLite {
        String id;
        String name;
        String imageUrl;
        Integer quantity; //for inventory & deck
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data @AllArgsConstructor
    public static class SearchResults {
        List<CardLite> data;
        Integer totalPages;
        Integer totalElements;
        String responseTime;
    }
    @Data
    @AllArgsConstructor
    public static class SetLite {
        String id;
        String name;
        String abbr;
    }
    @Data @AllArgsConstructor
    public static class FormatLite {
        String id;
        String name;
        String description;
    }

}
