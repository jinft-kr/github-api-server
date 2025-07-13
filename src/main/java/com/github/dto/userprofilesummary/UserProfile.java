package com.github.dto.userprofilesummary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserProfile {
    @JsonProperty("login")
    private String login;

    @JsonProperty("html_url")
    private String profileUrl;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("public_repos")
    private int publicRepos;
}