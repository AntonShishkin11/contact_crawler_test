package com.example.contact_crawler.crawler;

import java.util.Set;

public record CrawlResult(
        Set<String> links,
        Set<String> emails,
        Set<String> phones
) {
}
