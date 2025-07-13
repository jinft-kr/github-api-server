package com.github.dto.userprofilesummary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Repo {
    @JsonProperty("language")
    private String language;
}