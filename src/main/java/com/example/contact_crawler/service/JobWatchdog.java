package com.example.contact_crawler.service;

import com.example.contact_crawler.model.JobStatus;
import com.example.contact_crawler.repository.CrawlJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JobWatchdog {

    private final CrawlJobRepository repository;

    @Scheduled(fixedDelay = 60000)
    public void markStuckJobsAsFailed() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);

        repository.findByStatusAndStartedAtBefore(JobStatus.RUNNING, cutoff)
                .forEach(job -> {
                    job.setStatus(JobStatus.FAILED);
                    job.setFinishedAt(LocalDateTime.now());
                    repository.save(job);
                });
    }
}
