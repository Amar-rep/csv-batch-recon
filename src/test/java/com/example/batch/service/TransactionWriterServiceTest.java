package com.example.batch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.database.JdbcBatchItemWriter;

import com.example.batch.dto.RecordValidationStatus;
import com.example.batch.dto.TransactionData;
import com.example.batch.dto.TransactionProcessingResult;
import com.example.batch.model.Transaction;
import com.example.batch.repository.ReconciliationAuditRepository;
import com.example.batch.repository.TransactionRepository;
import com.example.batch.writer.TransactionErrorCsvWriter;

@ExtendWith(MockitoExtension.class)
public class TransactionWriterServiceTest {

    private static final Long RUN_ID = 5L;

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private JdbcBatchItemWriter<Transaction> jdbcWriter;
    @Mock
    private TransactionErrorCsvWriter csvWriter;
    @Mock
    private ReconciliationAuditRepository auditRepository;

    @Captor
    private ArgumentCaptor<Chunk<Transaction>> chunkCaptor;
    @Captor
    private ArgumentCaptor<List<TransactionProcessingResult>> errorsCaptor;

    private TransactionWriterService service;

    @BeforeEach
    void setUp() {
        service = new TransactionWriterService(transactionRepository, jdbcWriter, csvWriter, auditRepository);
    }

    private TransactionProcessingResult valid(String id) {
        TransactionData data = new TransactionData(4L, id, "ACC1", "2024-01-01", "10.50", "USD", "CREDIT", "desc");
        return new TransactionProcessingResult(data, RecordValidationStatus.VALID, null);
    }

    private TransactionProcessingResult invalid(String id) {
        TransactionData data = new TransactionData(4L, id, "ACC1", "2024-01-01", "10.50", "USD", "CREDIT", "desc");
        return new TransactionProcessingResult(data, RecordValidationStatus.INVALID, "bad");
    }

    @Test
    void validUniqueRecordsAreInserted() throws Exception {
        when(transactionRepository.findExistingTransactionIds(RUN_ID, Set.of("T1", "T2")))
                .thenReturn(Set.of());

        service.write(RUN_ID, List.of(valid("T1"), valid("T2")));

        verify(jdbcWriter).write(chunkCaptor.capture());
        List<Transaction> items = chunkCaptor.getValue().getItems();
        assertThat(items).extracting(Transaction::getTransactionId).containsExactly("T1", "T2");
        Transaction first = items.get(0);
        assertThat(first.getRunId()).isEqualTo(RUN_ID);
        assertThat(first.getAccountNumber()).isEqualTo("ACC1");
        assertThat(first.getTransactionDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(first.getAmount()).isEqualByComparingTo(new BigDecimal("10.50"));
        assertThat(first.getCurrency()).isEqualTo("USD");
        assertThat(first.getRowNumber()).isEqualTo(4L);
        verify(auditRepository).incrementCounters(RUN_ID, 0L, 0L, 0L, 0L, 0L);
        
    }

    @Test
    void invalidRecordsAreReportedAndCounted() throws Exception {
        service.write(RUN_ID, List.of(invalid("T1")));

     //   verify(transactionRepository, never()).findExistingTransactionIds(any(), any());
        verify(jdbcWriter).write(chunkCaptor.capture());
        assertThat(chunkCaptor.getValue().getItems()).isEmpty();
        verify(auditRepository).incrementCounters(RUN_ID, 0L, 0L, 1L, 0L, 0L);
        verify(csvWriter).writeTransactionErrors(errorsCaptor.capture(), eq(RUN_ID));
        assertThat(errorsCaptor.getValue().get(0).getValidationStatus()).isEqualTo(RecordValidationStatus.INVALID);
    }

    @Test
    void duplicateWithinChunkIsFlagged() throws Exception {
        when(transactionRepository.findExistingTransactionIds(RUN_ID, Set.of("T1"))).thenReturn(Set.of());

        service.write(RUN_ID, List.of(valid("T1"), valid("T1")));

        verify(jdbcWriter).write(chunkCaptor.capture());
        assertThat(chunkCaptor.getValue().getItems()).hasSize(1);
        verify(auditRepository).incrementCounters(RUN_ID, 0L, 0L, 0L, 1L, 0L);
        verify(csvWriter).writeTransactionErrors(errorsCaptor.capture(), eq(RUN_ID));
        assertThat(errorsCaptor.getValue()).hasSize(1);
        assertThat(errorsCaptor.getValue().get(0).getValidationStatus()).isEqualTo(RecordValidationStatus.DUPLICATE);
    }

    @Test
    void duplicateAlreadyInDatabaseIsFlagged() throws Exception {
        when(transactionRepository.findExistingTransactionIds(RUN_ID, Set.of("T1"))).thenReturn(Set.of("T1"));

        service.write(RUN_ID, List.of(valid("T1")));

        verify(jdbcWriter).write(chunkCaptor.capture());
        assertThat(chunkCaptor.getValue().getItems()).isEmpty();
        verify(auditRepository).incrementCounters(RUN_ID, 0L, 0L, 0L, 1L, 0L);
        verify(csvWriter).writeTransactionErrors(errorsCaptor.capture(), eq(RUN_ID));
        assertThat(errorsCaptor.getValue().get(0).getValidationStatus()).isEqualTo(RecordValidationStatus.DUPLICATE);
        assertThat(errorsCaptor.getValue().get(0).getErrorMessage())
                .isEqualTo("Transaction ID already exists for this job instance");
    }

    @Test
    void insertFailureIsWrapped() throws Exception {
        doThrow(new RuntimeException("db")).when(jdbcWriter).write(any());

        assertThatThrownBy(() -> service.write(RUN_ID, List.of(valid("T1"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to insert transactions for runId=5")
                .hasCauseInstanceOf(RuntimeException.class);
        verifyNoInteractions(auditRepository, csvWriter);
    }

    @Test
    void csvFailureIsWrapped() throws Exception {
        IOException cause = new IOException("disk");
        doThrow(cause).when(csvWriter).writeTransactionErrors(anyList(), eq(RUN_ID));

        assertThatThrownBy(() -> service.write(RUN_ID, List.of(invalid("T1"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to write transaction error CSV")
                .hasCause(cause);
    }
}