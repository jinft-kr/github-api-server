package com.github.dto.userprofilesummary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LanguageDistribution {
    @JsonProperty("language")
    private String language;
    @JsonProperty("percentage")
    private double percentage;
}
