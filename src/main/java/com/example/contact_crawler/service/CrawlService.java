package com.example.contact_crawler.service;

import com.example.contact_crawler.crawler.CrawlResult;
import com.example.contact_crawler.crawler.SimpleCrawler;
import com.example.contact_crawler.dto.CrawlJobResponse;
import com.example.contact_crawler.exception.NotFoundException;
import com.example.contact_crawler.mapper.CrawlJobMapper;
import com.example.contact_crawler.model.CrawlJob;
import com.example.contact_crawler.model.JobStatus;
import com.example.contact_crawler.model.ParsedRecord;
import com.example.contact_crawler.model.RecordType;
import com.example.contact_crawler.repository.CrawlJobRepository;
import com.example.contact_crawler.repository.ParsedRecordRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class CrawlService {

    private final CrawlJobRepository repository;
    private final ParsedRecordRepository recordRepository;
    private final ExecutorService crawlExecutor;
    private final MeterRegistry meterRegistry;

    private final Counter jobsSuccess;
    private final Counter jobsError;
    private final Counter recordsSaved;
    private final Timer jobTimer;
    private final Timer urlTimer;

    private static final int MAX_RUNNING_JOBS = 2;

    public CrawlService(
            CrawlJobRepository repository,
            ParsedRecordRepository recordRepository,
            ExecutorService crawlExecutor,
            MeterRegistry meterRegistry
    ) {
        this.repository = repository;
        this.recordRepository = recordRepository;
        this.crawlExecutor = crawlExecutor;
        this.meterRegistry = meterRegistry;

        this.jobsSuccess = Counter.builder("crawler_jobs_success_total")
                .description("Number of successful crawl jobs")
                .register(meterRegistry);

        this.jobsError = Counter.builder("crawler_jobs_error_total")
                .description("Number of failed crawl jobs")
                .register(meterRegistry);

        this.recordsSaved = Counter.builder("crawler_records_saved_total")
                .description("Number of parsed records saved to DB")
                .register(meterRegistry);

        this.jobTimer = Timer.builder("crawler_job_duration")
                .description("Total crawl job duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.urlTimer = Timer.builder("crawler_url_duration")
                .description("Single URL crawl duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        Gauge.builder("crawler_running_jobs", repository,
                        r -> r.countByStatus(JobStatus.RUNNING))
                .description("Number of RUNNING jobs")
                .register(meterRegistry);
    }

    /* =========================
       CREATE
       ========================= */
    public CrawlJobResponse createJob(String seedUrls) {
        CrawlJob job = new CrawlJob();
        job.setCreatedAt(LocalDateTime.now());
        job.setStatus(JobStatus.QUEUED);
        job.setProgress(0);
        job.setSeedUrls(seedUrls);

        CrawlJob saved = repository.save(job);
        tryStartNextJob();

        return CrawlJobMapper.toResponse(saved);
    }

    /* =========================
       READ
       ========================= */
    public CrawlJobResponse getJob(Long id) {
        return repository.findById(id)
                .map(CrawlJobMapper::toResponse)
                .orElseThrow(() ->
                        new NotFoundException("Crawl job not found: " + id)
                );
    }

    public Page<CrawlJobResponse> getJobs(int page, int size) {
        return repository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(page, size)
        ).map(CrawlJobMapper::toResponse);
    }

    /* =========================
       PROCESS
       ========================= */
    void processJob(Long jobId) {
        Timer.Sample jobSample = Timer.start(meterRegistry);

        try {
            CrawlJob job = repository.findById(jobId).orElseThrow();

            if (job.getStatus() != JobStatus.QUEUED) {
                return;
            }

            job.setStatus(JobStatus.RUNNING);
            job.setStartedAt(LocalDateTime.now());
            job.setProgress(0);
            repository.save(job);

            SimpleCrawler crawler = new SimpleCrawler();

            String[] urls = job.getSeedUrls().split(",");
            int total = urls.length;
            int processed = 0;

            for (String url : urls) {
                if (processed % 3 == 0) {
                    job = repository.findById(jobId).orElseThrow();
                }

                if (job.getStatus() == JobStatus.CANCELLED) {
                    return;
                }

                String normalized = url.trim();
                Timer.Sample urlSample = Timer.start(meterRegistry);

                try {
                    CrawlResult result = crawler.crawl(normalized);
                    urlSample.stop(urlTimer);

                    List<ParsedRecord> toSave = new ArrayList<>(
                            result.links().size()
                                    + result.emails().size()
                                    + result.phones().size()
                    );

                    result.links().forEach(v ->
                            toSave.add(new ParsedRecord(jobId, RecordType.LINK, v))
                    );
                    result.emails().forEach(v ->
                            toSave.add(new ParsedRecord(jobId, RecordType.EMAIL, v))
                    );
                    result.phones().forEach(v ->
                            toSave.add(new ParsedRecord(jobId, RecordType.PHONE, v))
                    );

                    if (!toSave.isEmpty()) {
                        recordRepository.saveAll(toSave);
                        recordsSaved.increment(toSave.size());
                    }

                } catch (IOException e) {
                    jobsError.increment();
                }

                processed++;
                job.setProgress((processed * 100) / total);

                if (processed % 3 == 0 || processed == total) {
                    repository.save(job);
                }
            }

            job = repository.findById(jobId).orElseThrow();
            job.setStatus(JobStatus.DONE);
            job.setFinishedAt(LocalDateTime.now());
            job.setProgress(100);
            repository.save(job);

            jobsSuccess.increment();

        } catch (Exception e) {
            CrawlJob job = repository.findById(jobId).orElse(null);
            if (job != null) {
                job.setStatus(JobStatus.FAILED);
                job.setFinishedAt(LocalDateTime.now());
                repository.save(job);
            }
            jobsError.increment();
        } finally {
            jobSample.stop(jobTimer);
            tryStartNextJob();
        }
    }

    /* =========================
       QUEUE MANAGEMENT
       ========================= */
    private synchronized void tryStartNextJob() {
        long runningCount = repository.countByStatus(JobStatus.RUNNING);
        if (runningCount >= MAX_RUNNING_JOBS) {
            return;
        }

        repository.findFirstByStatusOrderByCreatedAtAsc(JobStatus.QUEUED)
                .ifPresent(job ->
                        crawlExecutor.submit(() -> processJob(job.getId()))
                );
    }

    /* =========================
       CANCEL
       ========================= */
    public CrawlJobResponse cancelJob(Long id) {
        CrawlJob job = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Crawl job not found: " + id));

        if (job.getStatus() == JobStatus.DONE || job.getStatus() == JobStatus.FAILED) {
            throw new IllegalStateException(
                    "Cannot cancel job in status " + job.getStatus()
            );
        }

        job.setStatus(JobStatus.CANCELLED);
        job.setFinishedAt(LocalDateTime.now());
        repository.save(job);

        return CrawlJobMapper.toResponse(job);
    }

    /* =========================
       RETRY
       ========================= */
    public CrawlJobResponse retryJob(Long id) {
        CrawlJob job = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Crawl job not found: " + id));

        if (job.getStatus() != JobStatus.FAILED) {
            throw new IllegalStateException(
                    "Retry allowed only for FAILED jobs, current status: " + job.getStatus()
            );
        }

        job.setStatus(JobStatus.QUEUED);
        job.setStartedAt(null);
        job.setFinishedAt(null);
        job.setProgress(0);

        repository.save(job);
        tryStartNextJob();

        return CrawlJobMapper.toResponse(job);
    }
}
