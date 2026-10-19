package com.example.batch.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.batch.dto.ReconciliationResult;
import com.example.batch.model.Transaction;
import com.example.batch.repository.ReconciliationAuditRepository;
import com.example.batch.repository.TransactionRepository;
import com.example.batch.writer.TransactionErrorCsvWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReconciliationWriterService {

	private final TransactionRepository transactionRepository;
	private final ReconciliationAuditRepository reconciliationAuditRepository;
	private final TransactionErrorCsvWriter reconciliationCsvWriter;

	@Transactional
	public void write(Long runId, List<? extends ReconciliationResult> chunkList) {

		List<ReconciliationResult> csvResults = new ArrayList<>();
		List<Transaction> transactionsToUpdate = new ArrayList<>();
		long matchedCount = 0L;
		long mismatchedCount = 0L;
		long invalidCount = 0L;
		long missingFromFirst = 0L;
		for (ReconciliationResult result : chunkList) {

			switch (result.getStatus()) {
			case INVALID -> {
				invalidCount++;
				csvResults.add(result);
			}
			case MISSING -> {
				missingFromFirst++;
				csvResults.add(result);
			}
			case AMOUNT_MISMATCH -> {
				mismatchedCount++;
				Transaction existing = result.getFile1Transaction();
				BigDecimal file2Amount = new BigDecimal(result.getOriginalRecord().getAmount());
				existing.setAmount(file2Amount);
				existing.setUpdatedAt(LocalDateTime.now());
				transactionsToUpdate.add(existing);
				csvResults.add(result);
			}
			case MATCHED -> matchedCount++;
			}
		}

		if (!transactionsToUpdate.isEmpty()) {
			transactionRepository.saveAll(transactionsToUpdate);
		}

		reconciliationAuditRepository.incrementCounters(runId, matchedCount, mismatchedCount, invalidCount, 0L,
				missingFromFirst);

		if (!csvResults.isEmpty()) {
			try {
				reconciliationCsvWriter.writeReconciliationErrors(csvResults, runId);
			} catch (IOException e) {
				log.error("Failed to write reconciliation CSV for runId={}", runId, e);

				throw new IllegalStateException("Failed to write reconciliation CSV for runId=" + runId, e);
			}
		}
	}
}
