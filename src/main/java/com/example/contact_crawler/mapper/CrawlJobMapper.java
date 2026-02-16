package com.example.contact_crawler.mapper;

import com.example.contact_crawler.dto.CrawlJobResponse;
import com.example.contact_crawler.model.CrawlJob;

public class CrawlJobMapper {

    private CrawlJobMapper() {
    }

    public static CrawlJobResponse toResponse(CrawlJob job) {
        return new CrawlJobResponse(
                job.getId(),
                job.getCreatedAt(),
                job.getStartedAt(),
                job.getFinishedAt(),
                job.getStatus(),
                job.getProgress(),
                job.getSeedUrls()
        );
    }

}
