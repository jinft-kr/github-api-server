package com.github.dto.reposummary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RepoSummaryResponse {

    @JsonProperty("owner")
    private String owner;
    @JsonProperty("repo")
    private String repo;
    @JsonProperty("repo_info")
    private RepoInfo repoInfo;
    @JsonProperty("issue_stats")
    private IssueStats issueStats;
    @JsonProperty("pull_request_stats")
    private PullRequestStats pullRequestStats;
    @JsonProperty("last_updated_utc")
    private String lastUpdatedUtc;
}