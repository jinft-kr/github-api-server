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
public class TopActiveReviewer {

    @JsonProperty("username")
    private String username;
    @JsonProperty("reviewed_prs_count")
    private int reviewedPrsCount;
}