package com.github.dto.reposummary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoInfo {

    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("forks")
    private int forks;
    @JsonProperty("default_branch")
    private String defaultBranch;
    @JsonProperty("total_branches")
    private int totalBranches;
    @JsonProperty("total_commits")
    private int totalCommits;
    @JsonProperty("last_updated_utc")
    private String latestCommitDateUtc;
    @JsonProperty("size")
    private int size;
}