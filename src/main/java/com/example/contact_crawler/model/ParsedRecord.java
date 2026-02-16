package com.example.contact_crawler.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "parsed_record",
        indexes = {
                @Index(name = "idx_record_job_id", columnList = "jobId"),
                @Index(name = "idx_record_type", columnList = "type")
        }
)
public class ParsedRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobId;

    @Enumerated(EnumType.STRING)
    private RecordType type;


    @Column(name = "record_value", length = 2048)
    private String value;

    public ParsedRecord(Long jobId, RecordType type, String value) {
        this.jobId = jobId;
        this.type = type;
        this.value = value;
    }
}
