package com.example.contact_crawler.repository;

import com.example.contact_crawler.model.ParsedRecord;
import com.example.contact_crawler.model.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ParsedRecordRepository extends JpaRepository<ParsedRecord, Long> {

    long countByJobId(Long jobId);

    long countByType(RecordType type);

    @Query("select count(pr) from ParsedRecord pr")
    long countAll();
}
