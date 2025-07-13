package com.github.dto.reposummary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PullRequestStats {

    private int totalPrs;
    private int openPrs;
    private int closedPrs;
    private int mergedPrs;
    private double avgTimeToMergePrHours;
    private List<TopActiveReviewer> topActiveReviewers;
}