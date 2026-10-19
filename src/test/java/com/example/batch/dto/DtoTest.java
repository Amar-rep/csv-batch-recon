package com.example.batch.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.example.batch.model.Transaction;

public class DtoTest {

	@Test
	void transactionDataAllArgsAndSetters() {
		TransactionData data = new TransactionData(5L, "T1", "A1", "2024-01-01", "10.5", "USD", "CREDIT", "d");

		assertThat(data.getRowNumber()).isEqualTo(5L);
		assertThat(data.getTransactionId()).isEqualTo("T1");
		assertThat(data.getAccountNumber()).isEqualTo("A1");
		assertThat(data.getTransactionDate()).isEqualTo("2024-01-01");
		assertThat(data.getAmount()).isEqualTo("10.5");
		assertThat(data.getCurrency()).isEqualTo("USD");
		assertThat(data.getTransactionType()).isEqualTo("CREDIT");
		assertThat(data.getDescription()).isEqualTo("d");

		TransactionData other = new TransactionData();
		other.setRowNumber(6L);
		other.setTransactionId("T2");
		other.setAccountNumber("A2");
		other.setTransactionDate("2024-02-02");
		other.setAmount("1");
		other.setCurrency("EUR");
		other.setTransactionType("DEBIT");
		other.setDescription("x");

		assertThat(other.getRowNumber()).isEqualTo(6L);
		assertThat(other.getTransactionId()).isEqualTo("T2");
		assertThat(other.getAccountNumber()).isEqualTo("A2");
		assertThat(other.getTransactionDate()).isEqualTo("2024-02-02");
		assertThat(other.getAmount()).isEqualTo("1");
		assertThat(other.getCurrency()).isEqualTo("EUR");
		assertThat(other.getTransactionType()).isEqualTo("DEBIT");
		assertThat(other.getDescription()).isEqualTo("x");
	}

	@Test
	void errorCsvDtoSettersAndGetters() {
		TransactionData data = new TransactionData();
		ErrorCsvDto dto = new ErrorCsvDto();
		dto.setData(data);
		dto.setStatus("INVALID");
		dto.setMessage("bad");

		assertThat(dto.getData()).isSameAs(data);
		assertThat(dto.getStatus()).isEqualTo("INVALID");
		assertThat(dto.getMessage()).isEqualTo("bad");
	}

	@Test
	void reconciliationResultAllArgsAndSetters() {
		TransactionData data = new TransactionData();
		Transaction tx = new Transaction();

		ReconciliationResult result = new ReconciliationResult(data, ReconciliationStatus.MATCHED, "r", tx);
		assertThat(result.getOriginalRecord()).isSameAs(data);
		assertThat(result.getStatus()).isEqualTo(ReconciliationStatus.MATCHED);
		assertThat(result.getReason()).isEqualTo("r");
		assertThat(result.getFile1Transaction()).isSameAs(tx);

		ReconciliationResult empty = new ReconciliationResult();
		empty.setOriginalRecord(data);
		empty.setStatus(ReconciliationStatus.MISSING);
		empty.setReason("m");
		empty.setFile1Transaction(tx);
		assertThat(empty.getOriginalRecord()).isSameAs(data);
		assertThat(empty.getStatus()).isEqualTo(ReconciliationStatus.MISSING);
		assertThat(empty.getReason()).isEqualTo("m");
		assertThat(empty.getFile1Transaction()).isSameAs(tx);
	}

	@Test
	void transactionProcessingResultAllArgsAndSetters() {
		TransactionData data = new TransactionData();

		TransactionProcessingResult result = new TransactionProcessingResult(data, RecordValidationStatus.VALID, null);
		assertThat(result.getTransactionData()).isSameAs(data);
		assertThat(result.getValidationStatus()).isEqualTo(RecordValidationStatus.VALID);
		assertThat(result.getErrorMessage()).isNull();

		TransactionProcessingResult empty = new TransactionProcessingResult();
		empty.setTransactionData(data);
		empty.setValidationStatus(RecordValidationStatus.DUPLICATE);
		empty.setErrorMessage("dup");
		assertThat(empty.getTransactionData()).isSameAs(data);
		assertThat(empty.getValidationStatus()).isEqualTo(RecordValidationStatus.DUPLICATE);
		assertThat(empty.getErrorMessage()).isEqualTo("dup");
	}

	@Test
	void enumsExposeExpectedConstants() {
		assertThat(ReconciliationStatus.values()).extracting(Enum::name).containsExactly("MATCHED", "AMOUNT_MISMATCH",
				"MISSING", "INVALID");
		assertThat(RecordValidationStatus.values()).extracting(Enum::name).containsExactly("VALID", "INVALID",
				"DUPLICATE", "MATCHED", "MISSING", "AMOUNT_MISMATCH");
	}

}