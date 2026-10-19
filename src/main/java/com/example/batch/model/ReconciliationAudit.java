package com.example.batch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reconciliation_audit", uniqueConstraints = @UniqueConstraint(name = "uk_reconciliation_audit_run_id", columnNames = "run_id"))
@Getter
@Setter
@NoArgsConstructor
public class ReconciliationAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "run_id", nullable = false, unique = true)
	private Long runId;

	@Column(name = "matched_records", nullable = false)
	private Long matchedRecords = 0L;

	@Column(name = "mismatched_records", nullable = false)
	private Long mismatchedRecords = 0L;

	@Column(name = "duplicate_records", nullable = false)
	private Long duplicateRecords = 0L;

	@Column(name = "invalid_records", nullable = false)
	private Long invalidRecords = 0L;

	@Column(name = "missing_from_first", nullable = false)
	private Long missing_from_first = 0L;

}