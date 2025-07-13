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
public class TopCommentedIssue {

    @JsonProperty("issue_number")
    private int issueNumber;
    @JsonProperty("title")
    private String title;
    @JsonProperty("comments_count")
    private int commentsCount;
}