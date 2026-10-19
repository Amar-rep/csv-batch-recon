package com.example.batch.processor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.batch.dto.RecordValidationStatus;
import com.example.batch.dto.TransactionData;
import com.example.batch.dto.TransactionProcessingResult;

public class TransactionProcessorTest {

    private final TransactionProcessor processor = new TransactionProcessor();

    private TransactionData data(String id, String account, String date, String amount, String currency,
            String type, String description) {
        TransactionData d = new TransactionData();
        d.setTransactionId(id);
        d.setAccountNumber(account);
        d.setTransactionDate(date);
        d.setAmount(amount);
        d.setCurrency(currency);
        d.setTransactionType(type);
        d.setDescription(description);
        return d;
    }

    @Test
    void nullTransactionIsInvalid() {
        TransactionProcessingResult result = processor.process(null);

        assertThat(result.getValidationStatus()).isEqualTo(RecordValidationStatus.INVALID);
        assertThat(result.getErrorMessage()).isEqualTo("Transaction data is null");
        assertThat(result.getTransactionData()).isNull();
    }

    @Test
    void validTransactionIsTrimmed() {
        TransactionData input = data("  T1 ", " ACC1", "2024-01-01 ", " 10.5 ", " USD ", " CREDIT ", "  desc  ");

        TransactionProcessingResult result = processor.process(input);

        assertThat(result.getValidationStatus()).isEqualTo(RecordValidationStatus.VALID);
        assertThat(result.getErrorMessage()).isNull();
        assertThat(result.getTransactionData()).isSameAs(input);
        assertThat(input.getTransactionId()).isEqualTo("T1");
        assertThat(input.getAccountNumber()).isEqualTo("ACC1");
        assertThat(input.getTransactionDate()).isEqualTo("2024-01-01");
        assertThat(input.getAmount()).isEqualTo("10.5");
        assertThat(input.getCurrency()).isEqualTo("USD");
        assertThat(input.getTransactionType()).isEqualTo("CREDIT");
        assertThat(input.getDescription()).isEqualTo("desc");
    }

   

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
            "NULL, ACC1, 2024-01-01, 10, USD, CREDIT, Invalid transaction ID",
            "'  ', ACC1, 2024-01-01, 10, USD, CREDIT, Invalid transaction ID",
            "T1, NULL, 2024-01-01, 10, USD, CREDIT, Invalid account number",
            "T1, '  ', 2024-01-01, 10, USD, CREDIT, Invalid account number",
            "T1, ACC1, NULL, 10, USD, CREDIT, Invalid transaction date",
            "T1, ACC1, '  ', 10, USD, CREDIT, Invalid transaction date",
            "T1, ACC1, 01/01/2024, 10, USD, CREDIT, Invalid transaction date",
            "T1, ACC1, 2024-01-01, NULL, USD, CREDIT, Invalid transaction amount",
            "T1, ACC1, 2024-01-01, '  ', USD, CREDIT, Invalid transaction amount",
            "T1, ACC1, 2024-01-01, abc, USD, CREDIT, Invalid transaction amount",
            "T1, ACC1, 2024-01-01, -1, USD, CREDIT, Invalid transaction amount",
            "T1, ACC1, 2024-01-01, 10, NULL, CREDIT, Invalid currency",
            "T1, ACC1, 2024-01-01, 10, '  ', CREDIT, Invalid currency",
            "T1, ACC1, 2024-01-01, 10, USD, NULL, Invalid transaction type",
            "T1, ACC1, 2024-01-01, 10, USD, '  ', Invalid transaction type" })
    void invalidTransactions(String id, String account, String date, String amount, String currency,
            String type, String message) {
        TransactionData input = data(id, account, date, amount, currency, type, "d");

        TransactionProcessingResult result = processor.process(input);

        assertThat(result.getValidationStatus()).isEqualTo(RecordValidationStatus.INVALID);
        assertThat(result.getErrorMessage()).isEqualTo(message);
        assertThat(result.getTransactionData()).isSameAs(input);
    }
}