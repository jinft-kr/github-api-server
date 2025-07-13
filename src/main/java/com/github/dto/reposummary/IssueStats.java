package com.github.dto.reposummary;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueStats {

    @JsonProperty("total_issues")
    private int totalIssues;
    @JsonProperty("open_issues")
    private int openIssues;
    @JsonProperty("closed_issues")
    private int closedIssues;
    @JsonProperty("avg_time_to_close_issue_hours")
    private double avgTimeToCloseIssueHours;
    @JsonProperty("top_commented_issues")
    private List<TopCommentedIssue> topCommentedIssues;
}