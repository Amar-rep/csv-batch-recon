package com.example.batch.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.stereotype.Service;

import com.example.batch.dto.RecordValidationStatus;
import com.example.batch.dto.TransactionData;
import com.example.batch.dto.TransactionProcessingResult;
import com.example.batch.model.Transaction;
import com.example.batch.repository.ReconciliationAuditRepository;
import com.example.batch.repository.TransactionRepository;
import com.example.batch.writer.TransactionErrorCsvWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionWriterService {
	private final TransactionRepository transactionRepository;
	private final JdbcBatchItemWriter<Transaction> transactionBatchWriter;
	private final TransactionErrorCsvWriter errorCsvWriter;
	private final ReconciliationAuditRepository reconciliationAuditRespository;

	public void write(Long runId, List<? extends TransactionProcessingResult> chunkList) {
		List<TransactionProcessingResult> errorTransactionsList = new ArrayList<>();
		List<TransactionProcessingResult> validTransactionsList = new ArrayList<>();
		Set<String> seenIdSet = new HashSet<>();
		List<Transaction> transactionsToInsert = new ArrayList<>();
		Long invalidCount = 0L;
		Long duplicateCount = 0L;
		for (TransactionProcessingResult result : chunkList) {
			if (result.getValidationStatus() == RecordValidationStatus.INVALID) {
				invalidCount++;
				errorTransactionsList.add(result);
				continue;
			}
			if (!seenIdSet.add(result.getTransactionData().getTransactionId())) {
				errorTransactionsList.add(new TransactionProcessingResult(result.getTransactionData(),
						RecordValidationStatus.DUPLICATE, "Duplicate transaction ID within the current chunk"));
				duplicateCount++;
				continue;
			}
			validTransactionsList.add(result);
		}

		Set<String> duplicatesSet = seenIdSet.isEmpty() ? new HashSet<>()
				: transactionRepository.findExistingTransactionIds(runId, seenIdSet);

		for (TransactionProcessingResult result : validTransactionsList) {
			TransactionData data = result.getTransactionData();
			String transactionId = data.getTransactionId();

			if (duplicatesSet.contains(transactionId)) {
				errorTransactionsList.add(new TransactionProcessingResult(data, RecordValidationStatus.DUPLICATE,
						"Transaction ID already exists for this job instance"));
				duplicateCount++;
				continue;
			}

			transactionsToInsert.add(createTransaction(data, runId));
		}
		// transaction writing
		try {
			transactionBatchWriter.write(new Chunk<>(transactionsToInsert));
		} catch (Exception e) {
			log.error("Failed to insert transactions for runId={}", runId, e);
			throw new IllegalStateException("Failed to insert transactions for runId=" + runId, e);
		}
		// write to audit repo

		reconciliationAuditRespository.incrementCounters(runId, 0L, 0L, invalidCount, duplicateCount, 0L);
		// Error CSV writing
		if (!errorTransactionsList.isEmpty()) {
			try {
				errorCsvWriter.writeTransactionErrors(errorTransactionsList, runId);
			} catch (IOException e) {
				log.error("Failed to write error CSV for runId={}", runId, e);
				throw new IllegalStateException("Failed to write transaction error CSV", e);
			}
		}
	}

	private Transaction createTransaction(TransactionData data, Long runId) {

		LocalDateTime now = LocalDateTime.now();

		Transaction transaction = new Transaction();
		transaction.setRunId(runId);
		transaction.setTransactionId(data.getTransactionId());
		transaction.setAccountNumber(data.getAccountNumber());
		transaction.setTransactionDate(LocalDate.parse(data.getTransactionDate()));
		transaction.setAmount(new BigDecimal(data.getAmount()));
		transaction.setCurrency(data.getCurrency());
		transaction.setTransactionType(data.getTransactionType());
		transaction.setDescription(data.getDescription());
		transaction.setRowNumber(data.getRowNumber());
		transaction.setCreatedAt(now);
		transaction.setUpdatedAt(now);

		return transaction;
	}

}
