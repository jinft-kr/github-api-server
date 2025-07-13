package com.github.controller;

import com.github.dto.popularrepos.PopularRepoResponse;
import com.github.dto.reposummary.RepoSummaryResponse;
import com.github.dto.userprofilesummary.UserProfileResponse;
import com.github.service.PopularReposService;
import com.github.service.RepoSummaryService;
import com.github.service.UserProfileSummaryService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GithubApiController {

    private final RepoSummaryService repoService;
    private final UserProfileSummaryService userService;
    private final PopularReposService popularRepoService;

    @GetMapping("/repos/{owner}/{repo}/summary")
    @Tag(name = "저장소 활동 API", description = "특정 GitHub 저장소의 활동 통계를 요약 제공")
    public RepoSummaryResponse getRepoSummary(@PathVariable String owner,
                                              @PathVariable String repo,
                                              @Parameter(hidden = true) @RequestHeader(name = "Authorization", required = false) String authHeader) {
        log.info("[Request Header] Authorization: {}", authHeader);
        return repoService.getRepoSummary(owner, repo, authHeader);
    }

    @GetMapping("/users/{username}/profile-summary")
    @Tag(name = "사용자 프로필 분석 API", description = "특정 GitHub 사용자의 공개 활동 통계를 요약 제공")
    public ResponseEntity<UserProfileResponse> getUserProfileSummary(@PathVariable String username,
                                                                     @Parameter(hidden = true) @RequestHeader(name = "Authorization", required = false) String authHeader) {
        UserProfileResponse profileSummary = userService.getUserProfileSummary(username, authHeader);
        return ResponseEntity.ok(profileSummary);
    }

    @GetMapping("/popular-repo")
    @Tag(name = "인기 저장소 API", description = "특정 조직이 소유한 공개 저장소 중, 별표(stars) 수가 가장 많은 상위 N 개 저장소 목록 제공")
    public PopularRepoResponse getPopularRepos(@RequestParam String owner,
                                               @RequestParam(required = false, defaultValue = "3") int limit,
                                               @Parameter(hidden = true) @RequestHeader(name = "Authorization", required = false) String authHeader) {
        return popularRepoService.getPopularRepos(owner, limit, authHeader);
    }
}
