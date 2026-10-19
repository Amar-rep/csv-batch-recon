
package com.example.batch.processor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.example.batch.dto.RecordValidationStatus;
import com.example.batch.dto.TransactionData;
import com.example.batch.dto.TransactionProcessingResult;

@Component
public class TransactionProcessor implements ItemProcessor<TransactionData, TransactionProcessingResult> {

	@Override
	public TransactionProcessingResult process(TransactionData transaction) {

		if (transaction == null) {
			return invalid(null, "Transaction data is null");
		}

		transaction.setTransactionId(trim(transaction.getTransactionId()));
		transaction.setAccountNumber(trim(transaction.getAccountNumber()));
		transaction.setTransactionDate(trim(transaction.getTransactionDate()));
		transaction.setAmount(trim(transaction.getAmount()));
		transaction.setCurrency(trim(transaction.getCurrency()));
		transaction.setTransactionType(trim(transaction.getTransactionType()));
		transaction.setDescription(trim(transaction.getDescription()));
		/*
		 * if(transaction.getTransactionId().equals("TXN007115")) { throw new
		 * RuntimeException(); }
		 */

		if (!isValidTransactionId(transaction.getTransactionId())) {
			return invalid(transaction, "Invalid transaction ID");
		}

		if (!isValidAccountNumber(transaction.getAccountNumber())) {
			return invalid(transaction, "Invalid account number");
		}

		if (!isValidTransactionDate(transaction.getTransactionDate())) {
			return invalid(transaction, "Invalid transaction date");
		}

		if (!isValidAmount(transaction.getAmount())) {
			return invalid(transaction, "Invalid transaction amount");
		}

		if (!isValidCurrency(transaction.getCurrency())) {
			return invalid(transaction, "Invalid currency");
		}

		if (!isValidTransactionType(transaction.getTransactionType())) {
			return invalid(transaction, "Invalid transaction type");
		}

		return new TransactionProcessingResult(transaction, RecordValidationStatus.VALID, null);
	}

	private boolean isValidTransactionId(String transactionId) {
		return transactionId != null && !transactionId.isBlank();
	}

	private boolean isValidAccountNumber(String accountNumber) {
		return accountNumber != null && !accountNumber.isBlank();
	}

	private boolean isValidTransactionDate(String transactionDate) {
		if (transactionDate == null || transactionDate.isBlank()) {
			return false;
		}
		try {
			LocalDate.parse(transactionDate);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	private boolean isValidAmount(String amount) {
		if (amount == null || amount.isBlank()) {
			return false;
		}

		try {
			return new BigDecimal(amount).compareTo(BigDecimal.ZERO) >= 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private boolean isValidCurrency(String currency) {
		return currency != null && !currency.isBlank();
	}

	private boolean isValidTransactionType(String transactionType) {
		return transactionType != null && !transactionType.isBlank();
	}

	private String trim(String value) {
		return value == null ? null : value.trim();
	}

	private TransactionProcessingResult invalid(TransactionData data, String message) {

		return new TransactionProcessingResult(data, RecordValidationStatus.INVALID, message);
	}
}
