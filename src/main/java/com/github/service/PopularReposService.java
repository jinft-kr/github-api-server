package com.github.service;

import com.github.dto.popularrepos.PopularRepoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularReposService {

    private final WebClient webClient;

    public PopularRepoResponse getPopularRepos(String owner, int limit, String authHeader) {
        return fetchPopularRepos(owner, limit, authHeader);
    }

    public PopularRepoResponse fetchPopularRepos(String owner, int limit, String authHeader) {
        final int perPage = 100;
        int page = 1;
        List<Map<String, Object>> allRepos = new ArrayList<>();
        boolean hasMore = true;

        while (hasMore) {
            int currentPage = page;

            var request = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/orgs/{owner}/repos")
                            .queryParam("type", "public")
                            .queryParam("per_page", perPage)
                            .queryParam("page", currentPage)
                            .build(owner));

            if (authHeader != null && !authHeader.isBlank()) {
                request = request.header(HttpHeaders.AUTHORIZATION, authHeader);
            }

            List<Map<String, Object>> reposPage = request
                    .retrieve()
                    .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .collectList()
                    .block();

            if (reposPage == null || reposPage.isEmpty()) {
                hasMore = false;
            } else {
                allRepos.addAll(reposPage);
                hasMore = reposPage.size() == perPage;
                page++;
            }
        }

        List<PopularRepoResponse.PopularRepo> popularRepos = allRepos.stream()
                .sorted((r1, r2) -> Integer.compare(
                        (Integer) r2.getOrDefault("stargazers_count", 0),
                        (Integer) r1.getOrDefault("stargazers_count", 0)))
                .limit(limit)
                .map(repo -> PopularRepoResponse.PopularRepo.builder()
                        .name((String) repo.get("name"))
                        .description((String) repo.get("description"))
                        .stars((Integer) repo.getOrDefault("stargazers_count", 0))
                        .forks((Integer) repo.getOrDefault("forks_count", 0))
                        .mainLanguage((String) repo.get("language"))
                        .build())
                .toList();

        return PopularRepoResponse.builder()
                .targetOwner(owner)
                .totalPublicRepos(allRepos.size())
                .popularRepos(popularRepos)
                .lastUpdatedUtc(OffsetDateTime.now(ZoneOffset.UTC).toString())
                .build();
    }
}
