package com.lab.webcrawler.api.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SeedRequest(@NotEmpty List<String> urls) {
}
