package com.example.contact_crawler.dto;

import com.example.contact_crawler.model.JobStatus;
import java.time.LocalDateTime;

public record CrawlJobResponse(
        Long id,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        JobStatus status,
        Integer progress,
        String seedUrls
) {}

