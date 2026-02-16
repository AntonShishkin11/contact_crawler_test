package com.example.contact_crawler.repository;

import com.example.contact_crawler.model.CrawlJob;
import com.example.contact_crawler.model.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CrawlJobRepository extends JpaRepository<CrawlJob, Long> {

    long countByStatus(JobStatus status);

    Optional<CrawlJob> findFirstByStatusOrderByCreatedAtAsc(JobStatus status);

    List<CrawlJob> findByStatusAndStartedAtBefore(
            JobStatus status,
            LocalDateTime startedAt
    );

    Page<CrawlJob> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
