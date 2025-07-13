package com.github.dto.popularrepos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PopularRepoResponse {
    @JsonProperty("target_owner")
    private String targetOwner;
    @JsonProperty("total_public_repos")
    private int totalPublicRepos;
    @JsonProperty("popular_repos")
    private List<PopularRepo> popularRepos;
    @JsonProperty("last_updated_utc")
    private String lastUpdatedUtc;

    @Data
    @Builder
    public static class PopularRepo {
        @JsonProperty("name")
        private String name;
        @JsonProperty("description")
        private String description;
        @JsonProperty("stars")
        private int stars;
        @JsonProperty("forks")
        private int forks;
        @JsonProperty("main_language")
        private String mainLanguage;
    }
}
