package com.example.batch.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.batch.dto.ReconciliationResult;
import com.example.batch.dto.ReconciliationStatus;
import com.example.batch.dto.TransactionData;
import com.example.batch.model.Transaction;
import com.example.batch.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
public class ReconciliationProcessorTest {

    private static final Long RUN_ID = 1L;

    @Mock
    private TransactionRepository repository;

    private ReconciliationProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ReconciliationProcessor(repository);
        ReflectionTestUtils.setField(processor, "runId", RUN_ID);
    }

    private TransactionData data(String id, String account, String date, String amount, String currency,
            String type) {
        TransactionData d = new TransactionData();
        d.setTransactionId(id);
        d.setAccountNumber(account);
        d.setTransactionDate(date);
        d.setAmount(amount);
        d.setCurrency(currency);
        d.setTransactionType(type);
        return d;
    }

    private Transaction existing(String amount) {
        Transaction t = new Transaction();
        t.setAmount(new BigDecimal(amount));
        return t;
    }

    @Test
    void nullRecordIsInvalid() {
        ReconciliationResult result = processor.process(null);

        assertThat(result.getStatus()).isEqualTo(ReconciliationStatus.INVALID);
        assertThat(result.getReason()).isEqualTo("Record is null");
        assertThat(result.getOriginalRecord()).isNull();
        assertThat(result.getFile1Transaction()).isNull();
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
            "NULL, ACC1, 2024-01-01, 10, USD, CREDIT, Transaction ID is missing",
            "T1, NULL, 2024-01-01, 10, USD, CREDIT, Account number is missing",
            "T1, ACC1, NULL, 10, USD, CREDIT, Invalid transaction date",
            "T1, ACC1, 2024-01-01, NULL, USD, CREDIT, Invalid transaction amount",
            "T1, ACC1, 2024-01-01, -0.01, USD, CREDIT, Amount cannot be negative",
            "T1, ACC1, 2024-01-01, 10, NULL, CREDIT, Currency is missing",
            "T1, ACC1, 2024-01-01, 10, USD, '  ', Transaction type is missing" })
    void invalidRecords(String id, String account, String date, String amount, String currency, String type,
            String message) {
        TransactionData input = data(id, account, date, amount, currency, type);

        ReconciliationResult result = processor.process(input);

        assertThat(result.getStatus()).isEqualTo(ReconciliationStatus.INVALID);
        assertThat(result.getReason()).isEqualTo(message);
        assertThat(result.getOriginalRecord()).isSameAs(input);
        assertThat(result.getFile1Transaction()).isNull();
        verifyNoInteractions(repository);
    }

    @Test
    void missingWhenNotInFile1() {
        when(repository.findByRunIdAndTransactionId(RUN_ID, "T1")).thenReturn(Optional.empty());
        TransactionData input = data("T1", "ACC1", "2024-01-01", "10", "USD", "CREDIT");

        ReconciliationResult result = processor.process(input);

        assertThat(result.getStatus()).isEqualTo(ReconciliationStatus.MISSING);
        assertThat(result.getReason()).isEqualTo("Transaction not found in File 1");
        assertThat(result.getOriginalRecord()).isSameAs(input);
        assertThat(result.getFile1Transaction()).isNull();
    }

    @Test
    void amountMismatch() {
        Transaction existing = existing("10.00");
        when(repository.findByRunIdAndTransactionId(RUN_ID, "T1")).thenReturn(Optional.of(existing));

        ReconciliationResult result =
                processor.process(data("T1", "ACC1", "2024-01-01", "20.50", "USD", "CREDIT"));

        assertThat(result.getStatus()).isEqualTo(ReconciliationStatus.AMOUNT_MISMATCH);
        assertThat(result.getReason()).isEqualTo(" File 1: 10.00  File 2: 20.50");
        assertThat(result.getFile1Transaction()).isSameAs(existing);
    }

  
    void matchedIgnoresScale() {
        Transaction existing = existing("10");
        when(repository.findByRunIdAndTransactionId(RUN_ID, "T1")).thenReturn(Optional.of(existing));

        ReconciliationResult result =
                processor.process(data("T1", "ACC1", "2024-01-01", "10", "USD", "CREDIT"));

        assertThat(result.getStatus()).isEqualTo(ReconciliationStatus.MATCHED);
        assertThat(result.getReason()).isNull();
        assertThat(result.getFile1Transaction()).isSameAs(existing);
        verify(repository).findByRunIdAndTransactionId(RUN_ID, "T1");
    }
}