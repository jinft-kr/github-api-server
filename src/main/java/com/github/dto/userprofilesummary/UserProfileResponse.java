package com.github.dto.userprofilesummary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserProfileResponse {
    @JsonProperty("username")
    private String username;
    @JsonProperty("profile_url")
    private String profileUrl;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    @JsonProperty("public_repos")
    private int publicRepos;
    @JsonProperty("language_distribution")
    private List<LanguageDistribution> languageDistribution;
    @JsonProperty("last_updated_utc")
    private String lastUpdatedUtc;
}

