package com.example.contact_crawler.controller;

import com.example.contact_crawler.dto.CrawlJobResponse;
import com.example.contact_crawler.dto.CrawlRequest;
import com.example.contact_crawler.service.CrawlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/crawl")
public class CrawlController {

    private final CrawlService crawlService;

    @PostMapping
    public CrawlJobResponse start(@Valid @RequestBody CrawlRequest request) {
        return crawlService.createJob(
                String.join(",", request.seedUrls())
        );
    }

    @PostMapping("/{id}/cancel")
    public CrawlJobResponse cancel(@PathVariable Long id) {
        return crawlService.cancelJob(id);
    }

    @PostMapping("/{id}/retry")
    public CrawlJobResponse retry(@PathVariable Long id) {
        return crawlService.retryJob(id);
    }

    @GetMapping
    public Page<CrawlJobResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return crawlService.getJobs(page, size);
    }

    @GetMapping("/{id}")
    public CrawlJobResponse get(@PathVariable Long id) {
        return crawlService.getJob(id);
    }
}
