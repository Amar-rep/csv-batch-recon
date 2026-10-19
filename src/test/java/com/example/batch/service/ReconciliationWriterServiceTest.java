package com.example.batch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.batch.dto.ReconciliationResult;
import com.example.batch.dto.ReconciliationStatus;
import com.example.batch.dto.TransactionData;
import com.example.batch.model.Transaction;
import com.example.batch.repository.ReconciliationAuditRepository;
import com.example.batch.repository.TransactionRepository;
import com.example.batch.writer.TransactionErrorCsvWriter;

@ExtendWith(MockitoExtension.class)
public class ReconciliationWriterServiceTest {

	private static final Long RUN_ID = 9L;

	@Mock
	private TransactionRepository transactionRepository;
	@Mock
	private ReconciliationAuditRepository auditRepository;
	@Mock
	private TransactionErrorCsvWriter csvWriter;

	@Captor
	private ArgumentCaptor<List<Transaction>> transactionsCaptor;
	@Captor
	private ArgumentCaptor<List<ReconciliationResult>> resultsCaptor;
	
	@InjectMocks
	private ReconciliationWriterService service;
	
	private ReconciliationResult result(ReconciliationStatus status) {
		TransactionData data = new TransactionData();
		data.setAmount("20.00");
		Transaction existing = new Transaction();
		existing.setAmount(new BigDecimal("10.00"));
		return new ReconciliationResult(data, status, "reason", existing);
	}

	@ParameterizedTest
	@CsvSource({ "MATCHED, 1, 0, 0, 0, false", "INVALID, 0, 0, 1, 0, true", "MISSING, 0, 0, 0, 1, true",
			"AMOUNT_MISMATCH, 0, 1, 0, 0, true" })
	void countersAndCsvPerStatus(ReconciliationStatus status, long matched, long mismatched, long invalid, long missing,
			boolean csvExpected) throws Exception {
		service.write(RUN_ID, List.of(result(status)));

		verify(auditRepository).incrementCounters(RUN_ID, matched, mismatched, invalid, 0L, missing);
		verify(csvWriter, times(csvExpected ? 1 : 0)).writeReconciliationErrors(anyList(), eq(RUN_ID));
	}

	@Test
	void mismatchUpdatesExistingTransaction() {
		ReconciliationResult mismatch = result(ReconciliationStatus.AMOUNT_MISMATCH);
		Transaction existing = mismatch.getFile1Transaction();
		LocalDateTime before = LocalDateTime.now();

		service.write(RUN_ID, List.of(mismatch));

		verify(transactionRepository).saveAll(transactionsCaptor.capture());
		assertThat(transactionsCaptor.getValue()).containsExactly(existing);
		assertThat(existing.getAmount()).isEqualByComparingTo("20.00");
		assertThat(existing.getUpdatedAt()).isNotNull().isAfterOrEqualTo(before);
	}

	@Test
	void mixedStatusAggregated() throws Exception {
		List<ReconciliationResult> chunk = List.of(result(ReconciliationStatus.MATCHED),
				result(ReconciliationStatus.MATCHED), result(ReconciliationStatus.INVALID),
				result(ReconciliationStatus.MISSING), result(ReconciliationStatus.AMOUNT_MISMATCH));

		service.write(RUN_ID, chunk);

		verify(auditRepository).incrementCounters(RUN_ID, 2L, 1L, 1L, 0L, 1L);
		verify(csvWriter).writeReconciliationErrors(resultsCaptor.capture(), eq(RUN_ID));
		assertThat(resultsCaptor.getValue()).extracting(ReconciliationResult::getStatus).containsExactly(
				ReconciliationStatus.INVALID, ReconciliationStatus.MISSING, ReconciliationStatus.AMOUNT_MISMATCH);
	}

	@Test
	void onlyMatchedSkipsSaveAndCsv() throws Exception {
		service.write(RUN_ID, List.of(result(ReconciliationStatus.MATCHED)));

		verify(transactionRepository, never()).saveAll(anyList());
		verify(csvWriter, never()).writeReconciliationErrors(anyList(), eq(RUN_ID));
	}

	@Test
	void csvFailureIsWrapped() throws Exception {
		IOException cause = new IOException("disk");
		doThrow(cause).when(csvWriter).writeReconciliationErrors(anyList(), eq(RUN_ID));

		assertThatThrownBy(() -> service.write(RUN_ID, List.of(result(ReconciliationStatus.MISSING))))
				.isInstanceOf(IllegalStateException.class).hasMessage("Failed to write reconciliation CSV for runId=9")
				.hasCause(cause);
	}
}
