package com.example.batch.processor;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.batch.dto.ReconciliationResult;
import com.example.batch.dto.ReconciliationStatus;
import com.example.batch.dto.TransactionData;
import com.example.batch.model.Transaction;
import com.example.batch.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class ReconciliationProcessor implements ItemProcessor<TransactionData, ReconciliationResult> {

	private final TransactionRepository transactionRepository;

	@Value("#{stepExecution.jobExecution.jobInstance.id}")
	private Long runId;

	@Override
	public ReconciliationResult process(TransactionData data) {

		String validationError = validate(data);
		if (validationError != null) {
			return new ReconciliationResult(data, ReconciliationStatus.INVALID, validationError, null);
		}
		Transaction existing = transactionRepository.findByRunIdAndTransactionId(runId, data.getTransactionId())
				.orElse(null);
		if (existing == null) {
			return new ReconciliationResult(data, ReconciliationStatus.MISSING, "Transaction not found in File 1",
					null);
		}
		BigDecimal file2Amount = new BigDecimal(data.getAmount());

		if (existing.getAmount().compareTo(file2Amount) != 0) {
			return new ReconciliationResult(data, ReconciliationStatus.AMOUNT_MISMATCH,
					" File 1: " + existing.getAmount() + "  File 2: " + file2Amount, existing);
		}
		return new ReconciliationResult(data, ReconciliationStatus.MATCHED, null, existing);
	}

	private String validate(TransactionData data) {

		if (data == null) {
			return "Record is null";
		}

		if (data.getTransactionId() == null || data.getTransactionId().isBlank()) {
			return "Transaction ID is missing";
		}

		if (data.getAccountNumber() == null || data.getAccountNumber().isBlank()) {
			return "Account number is missing";
		}

		try {
			LocalDate.parse(data.getTransactionDate());
		} catch (Exception e) {
			return "Invalid transaction date";
		}

		try {
			BigDecimal amount = new BigDecimal(data.getAmount());

			if (amount.signum() < 0) {
				return "Amount cannot be negative";
			}
		} catch (Exception e) {
			return "Invalid transaction amount";
		}

		if (data.getCurrency() == null || data.getCurrency().isBlank()) {
			return "Currency is missing";
		}

		if (data.getTransactionType() == null || data.getTransactionType().isBlank()) {
			return "Transaction type is missing";
		}

		return null;
	}
}