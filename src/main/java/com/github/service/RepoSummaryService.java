package com.github.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.dto.reposummary.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepoSummaryService {

    private final WebClient webClient;

    public RepoSummaryResponse getRepoSummary(String owner, String repo, String authHeader) {

        RepoInfo repoInfo = fetchRepoInfo(owner, repo, authHeader);
        IssueStats issueStats = fetchIssueStats(owner, repo, authHeader);
        PullRequestStats pullRequestStats = fetchPullRequestStats(owner, repo, authHeader);

        return RepoSummaryResponse.builder()
                .owner(owner)
                .repo(repo)
                .repoInfo(repoInfo)
                .issueStats(issueStats)
                .pullRequestStats(pullRequestStats)
                .lastUpdatedUtc(OffsetDateTime.now(ZoneOffset.UTC).toString())
                .build();
    }

    private RepoInfo fetchRepoInfo(String owner, String repo, String authHeader) {
        log.info("1. {} header token", authHeader);

        var request = webClient.get()
                .uri("/repos/{owner}/{repo}", owner, repo);

        if (authHeader != null && !authHeader.isBlank()) {
            request = request.header(HttpHeaders.AUTHORIZATION, authHeader);
            log.info("2. {} header token", authHeader);
        }

        var repoJson = request.retrieve().bodyToMono(JsonNode.class).block();

        return RepoInfo.builder()
                .name(repoJson.path("name").asText())
                .description(repoJson.path("description").asText())
                .forks(repoJson.path("forks_count").asInt())
                .defaultBranch(repoJson.path("default_branch").asText())
                .size(repoJson.path("size").asInt())
                .totalBranches(fetchBranchCount(owner, repo, authHeader))
                .totalCommits(fetchCommitCount(owner, repo, repoJson.path("default_branch").asText(), authHeader))
                .latestCommitDateUtc(fetchLatestCommitDate(owner, repo, repoJson.path("default_branch").asText(), authHeader))
                .build();
    }

    private int fetchBranchCount(String owner, String repo, String authHeader) {
        int page = 1;
        int perPage = 100;
        int totalBranches = 0;

        while (true) {
            final int pageNum = page;

            var request = webClient.get()
                            .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/branches")
                            .queryParam("per_page", perPage)
                            .queryParam("page", pageNum)
                            .build(owner, repo));

            if (authHeader != null && !authHeader.isBlank()) {
                request = request.header(HttpHeaders.AUTHORIZATION, authHeader);
            }

            List<JsonNode> branches = request.retrieve().bodyToFlux(JsonNode.class).collectList().block();

            if (branches == null || branches.isEmpty()) {
                log.info("[Branch Fetch] page {}: no more branches found. Total branches so far: {}", page, totalBranches);
                break;
            }

            totalBranches += branches.size();

            log.info("[Branch Fetch] page {}: fetched {} branches. Total so far: {}", page, branches.size(), totalBranches);

            if (branches.size() < perPage) {
                log.info("[Branch Fetch] Reached last page at page {}. Final total branches: {}", page, totalBranches);
                break;
            }

            page++;
        }

        log.info("[Branch Fetch] Final total branch count: {}", totalBranches);
        return totalBranches;
    }

    private int fetchCommitCount(String owner, String repo, String defaultBranch, String authHeader) {
        int totalCount = 0;
        int page = 1;

        while (true) {
            int currentPage = page;

            log.info("[Commit Fetch] owner={}, repo={}, branch={}, page={}", owner, repo, defaultBranch, currentPage);

            var request = webClient.get()
                            .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/commits")
                            .queryParam("sha", defaultBranch)
                            .queryParam("per_page", 100)
                            .queryParam("page", currentPage)
                            .build(owner, repo));

            if (authHeader != null && !authHeader.isBlank()) {
                request = request.header(HttpHeaders.AUTHORIZATION, authHeader);
            }

            List<JsonNode> commits = request.retrieve().bodyToFlux(JsonNode.class).collectList().block();

            if (commits == null || commits.isEmpty()) {
                log.info("[Commit Fetch] 종료 - 페이지 {}에 커밋 없음", currentPage);
                break;
            }

            totalCount += commits.size();
            log.info("[Commit Fetch] 페이지 {} - 커밋 수: {}, 누적 커밋 수: {}", currentPage, commits.size(), totalCount);

            if (commits.size() < 100) {
                break;
            }

            page++;
        }

        log.info("[Commit Fetch] 최종 커밋 수: {}", totalCount);
        return totalCount;
    }

    private String fetchLatestCommitDate(String owner, String repo, String branch, String authHeader) {
        var request = webClient.get().uri("/repos/{owner}/{repo}/commits/{branch}", owner, repo, branch) ;

        if (authHeader != null && !authHeader.isBlank()) {
            request = request.header(HttpHeaders.AUTHORIZATION, authHeader);
        }

        JsonNode commitJson = request.retrieve().bodyToMono(JsonNode.class).block();

        return commitJson.path("commit").path("committer").path("date").asText();  // ISO 8601 형식
    }

    private IssueStats fetchIssueStats(String owner, String repo, String authHeader) {
        int openIssues = 0;
        int closedIssues = 0;
        List<Long> closeDurations = new ArrayList<>();
        List<TopCommentedIssue> allIssues = new ArrayList<>();

        int page = 1;
        int perPage = 100;

        while (true) {
            log.info("Fetching issues for {}/{} page {}", owner, repo, page);

            int finalPage = page;

            var request = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/issues")
                            .queryParam("state", "all")
                            .queryParam("per_page", perPage)
                            .queryParam("page", finalPage)
                            .build(owner, repo));

            if (authHeader != null && !authHeader.isBlank()) {
                request = request.header(HttpHeaders.AUTHORIZATION, authHeader);
            }

            List<JsonNode> issues = request.retrieve().bodyToFlux(JsonNode.class).collectList().block();

            if (issues == null || issues.isEmpty()) break;

            for (JsonNode issue : issues) {
                if (issue.has("pull_request")) continue; // PR 제외

                String state = issue.path("state").asText();
                if ("open".equals(state)) openIssues++;
                else if ("closed".equals(state)) closedIssues++;

                if (issue.hasNonNull("created_at") && issue.hasNonNull("closed_at")) {
                    try {
                        OffsetDateTime createdAt = OffsetDateTime.parse(issue.path("created_at").asText());
                        OffsetDateTime closedAt = OffsetDateTime.parse(issue.path("closed_at").asText());
                        closeDurations.add(Duration.between(createdAt, closedAt).toHours());
                    } catch (Exception e) {
                        log.warn("Error parsing dates for issue #{}: {}", issue.path("number").asInt(), e.getMessage());
                    }
                }

                allIssues.add(new TopCommentedIssue(
                        issue.path("number").asInt(),
                        issue.path("title").asText(),
                        issue.path("comments").asInt()
                ));
            }

            if (issues.size() < perPage) break;
            page++;
        }

        double avgTimeToClose = closeDurations.isEmpty() ? 0.0 :
                Math.round(closeDurations.stream().mapToLong(Long::longValue).average().orElse(0.0) * 100) / 100.0;

        List<TopCommentedIssue> top5 = allIssues.stream()
                .sorted(Comparator.comparingInt(TopCommentedIssue::getCommentsCount).reversed())
                .limit(5)
                .toList();

        log.info("Issue stats collected - Total: {}, Open: {}, Closed: {}, Avg close time: {}", openIssues + closedIssues, openIssues, closedIssues, avgTimeToClose);
        log.info("Top 5 commented issues: {}", top5);

        return IssueStats.builder()
                .totalIssues(openIssues + closedIssues)
                .openIssues(openIssues)
                .closedIssues(closedIssues)
                .avgTimeToCloseIssueHours(avgTimeToClose)
                .topCommentedIssues(top5)
                .build();
    }

    private PullRequestStats fetchPullRequestStats(String owner, String repo, String authHeader) {
        int open = 0, closed = 0, merged = 0;
        List<Long> mergeDurations = new ArrayList<>();
        Map<String, Integer> reviewerCounts = new HashMap<>();

        int page = 1;
        int perPage = 100;
        boolean hasMore = true;

        while (hasMore) {
            log.info("Fetching pull requests for repo: {}/{} page: {}", owner, repo, page);

            int finalPage = page;

            var request = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/pulls")
                            .queryParam("state", "all")
                            .queryParam("per_page", perPage)
                            .queryParam("page", finalPage)
                            .build(owner, repo));

            if (authHeader != null && !authHeader.isBlank()) {
                request = request.header(HttpHeaders.AUTHORIZATION, authHeader);
            }

            List<JsonNode> prs = request.retrieve().bodyToFlux(JsonNode.class).collectList().block();

            if (prs == null) {
                log.warn("No response (null) for page {}", page);
                break;
            }

            log.info("Fetched {} pull requests from page {}", prs.size(), page);

            if (prs.isEmpty()) {
                break;
            }

            for (JsonNode pr : prs) {
                String state = pr.path("state").asText();
                boolean isMerged = !pr.path("merged_at").isNull();

                if ("open".equals(state)) open++;
                else if ("closed".equals(state)) closed++;

                if (isMerged) {
                    merged++;
                    try {
                        OffsetDateTime created = OffsetDateTime.parse(pr.path("created_at").asText());
                        OffsetDateTime mergedAt = OffsetDateTime.parse(pr.path("merged_at").asText());
                        mergeDurations.add(Duration.between(created, mergedAt).toHours());
                        log.debug("PR #{} merged in {} hours", pr.path("number").asInt(), Duration.between(created, mergedAt).toHours());
                    } catch (Exception e) {
                        log.warn("Failed to parse merge dates for PR #{}: {}", pr.path("number").asInt(), e.getMessage());
                    }
                }

                JsonNode reviewers = pr.path("requested_reviewers");
                if (reviewers.isArray()) {
                    for (JsonNode reviewer : reviewers) {
                        String login = reviewer.path("login").asText();
                        reviewerCounts.put(login, reviewerCounts.getOrDefault(login, 0) + 1);
                    }
                }
            }

            page++;
            hasMore = prs.size() == perPage;
        }

        double avgMergeTime = mergeDurations.isEmpty() ? 0 : mergeDurations.stream().mapToLong(Long::longValue).average().orElse(0);

        List<TopActiveReviewer> topReviewers = reviewerCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(e -> new TopActiveReviewer(e.getKey(), e.getValue()))
                .toList();

        log.info("Total PRs: {}, Open: {}, Closed: {}, Merged: {}, Avg Merge Time: {}", open + closed, open, closed, merged, avgMergeTime);
        log.info("Top reviewers: {}", topReviewers);

        return PullRequestStats.builder()
                .totalPrs(open + closed)
                .openPrs(open)
                .closedPrs(closed)
                .mergedPrs(merged)
                .avgTimeToMergePrHours(Math.round(avgMergeTime * 10) / 10.0)
                .topActiveReviewers(topReviewers)
                .build();
    }

}
