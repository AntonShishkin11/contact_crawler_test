package com.example.contact_crawler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CrawlRequest(
        @NotEmpty(message = "seedUrls must not be empty")
        List<@NotBlank(message = "seedUrl must not be blank") String> seedUrls
) {}

