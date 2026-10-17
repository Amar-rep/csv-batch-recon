package com.example.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.batch.model.ReconciliationAudit;

public interface ReconciliationAuditRepository extends JpaRepository<ReconciliationAudit, Long> {

	@Modifying
	@Query("""
			UPDATE ReconciliationAudit r
			SET r.matchedRecords = r.matchedRecords + :matched,
			    r.mismatchedRecords = r.mismatchedRecords + :unmatched,
			    r.invalidRecords = r.invalidRecords + :invalid,
			    r.duplicateRecords = r.duplicateRecords + :duplicate,
			    r.missing_from_first=r.missing_from_first+:missing_from_first
			WHERE r.runId = :runId
			""")
	int incrementCounters(@Param("runId") Long runId, @Param("matched") Long matched,
			@Param("unmatched") Long unmatched, @Param("invalid") Long invalid, @Param("duplicate") Long duplicate,
			@Param("missing_from_first") Long missing_from_first);

	boolean existsByRunId(Long runId);
}
