package com.github.service;

import com.github.dto.userprofilesummary.LanguageDistribution;
import com.github.dto.userprofilesummary.Repo;
import com.github.dto.userprofilesummary.UserProfile;
import com.github.dto.userprofilesummary.UserProfileResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileSummaryService {

    private final WebClient webClient;

    public UserProfileResponse getUserProfileSummary(String username, String authHeader) {
            var userProfile = fetchUserProfile(username, authHeader);
            var languages = fetchUserLanguageStats(username, authHeader);

            return UserProfileResponse.builder()
                    .username(userProfile.getLogin())
                    .profileUrl(userProfile.getProfileUrl())
                    .avatarUrl(userProfile.getAvatarUrl())
                    .publicRepos(userProfile.getPublicRepos())
                    .languageDistribution(languages)
                    .lastUpdatedUtc(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                    .build();
    }

    private UserProfile fetchUserProfile(String username, String authHeader) {

        var request = webClient.get().uri("/users/{username}", username);

        if (authHeader != null && !authHeader.isBlank()) {
            request = request.header(HttpHeaders.AUTHORIZATION, authHeader);
        }

        return request.retrieve().bodyToMono(UserProfile.class).block();

    }

    private List<LanguageDistribution> fetchUserLanguageStats(String username, String authHeader) {

        Map<String, Integer> languageCount = new HashMap<>();
        int totalRepos = 0;

        int page = 1;
        int perPage = 100;
        boolean hasMore = true;

        while (hasMore) {
            log.info("Fetching repos for user: {} page: {}", username, page);

            // 각 페이지 별 요청
            int finalPage = page;

            var request = webClient.get().uri(uriBuilder -> uriBuilder
                            .path("/users/{username}/repos")
                            .queryParam("per_page", perPage)
                            .queryParam("page", finalPage)
                            .build(username)) ;

            if (authHeader != null && !authHeader.isBlank()) {
                request = request.header(HttpHeaders.AUTHORIZATION, authHeader);
            }

            List<Repo> repos = request.retrieve().bodyToFlux(Repo.class).collectList().block();

            if (repos == null) {
                log.warn("No response (null) for page {}", page);
                break;
            }

            log.info("Fetched {} repos from page {}", repos.size(), page);

            if (repos.isEmpty()) {
                hasMore = false;
                break;
            }

            totalRepos += repos.size();

            for (Repo repo : repos) {
                String lang = repo.getLanguage();
                if (lang == null || lang.isEmpty()) {
                    lang = "Other";  // 언어 미지정 저장소는 Other로 분류
                }
                languageCount.put(lang, languageCount.getOrDefault(lang, 0) + 1);
            }

            page++;
            hasMore = repos.size() == perPage; // 페이지에 모든 데이터가 있다면 다음 페이지도 있음
        }

        if (totalRepos == 0) {
            return Collections.emptyList();
        }

        // 비율 계산
        List<LanguageDistribution> result = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : languageCount.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / totalRepos;
            result.add(new LanguageDistribution(entry.getKey(), Math.round(percentage * 10) / 10.0)); // 소수점 1자리
        }

        // 내림차순 정렬
        result.sort((a, b) -> Double.compare(b.getPercentage(), a.getPercentage()));

        return result;
    }

}