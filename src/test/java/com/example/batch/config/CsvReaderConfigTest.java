package com.example.batch.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;

import com.example.batch.dto.TransactionData;

public class CsvReaderConfigTest {

    private final CsvReaderConfig config = new CsvReaderConfig();

    @TempDir
    Path dir;

    private Path csv() throws IOException {
        Path file = dir.resolve("in.csv");
        Files.writeString(file,
                "transaction_id,account_number,transaction_date,amount,currency,transaction_type,description\n"
                        + "T1,ACC1,2024-01-01,10.50,USD,CREDIT,first\n"
                        + "T2,ACC2,2024-01-02,20.00,EUR,DEBIT,second\n");
        return file;
    }

    private FlatFileItemReader<TransactionData> reader(String which, String path) {
        LineMapper<TransactionData> mapper = config.transactionLineMapper();
        return "file1".equals(which) ? config.file1Reader(path, mapper) : config.file2Reader(path, mapper);
    }

    @ParameterizedTest
    @ValueSource(strings = { "file1", "file2" })
    void readsRowsAndSkipsHeader(String which) throws Exception {
        FlatFileItemReader<TransactionData> reader = reader(which, csv().toString());
        ExecutionContext context = new ExecutionContext();
        reader.open(context);
        try {
            TransactionData first = reader.read();
            assertThat(first.getTransactionId()).isEqualTo("T1");
            assertThat(first.getRowNumber()).isEqualTo(2L);

            reader.update(context);

            TransactionData second = reader.read();
            assertThat(second.getTransactionId()).isEqualTo("T2");
            assertThat(second.getRowNumber()).isEqualTo(3L);

            assertThat(reader.read()).isNull();
        } finally {
            reader.close();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "file1", "file2" })
    void missingFileFailsInStrictMode(String which) {
        FlatFileItemReader<TransactionData> reader = reader(which, dir.resolve("missing.csv").toString());

        assertThatThrownBy(() -> reader.open(new ExecutionContext()))
                .isInstanceOf(ItemStreamException.class);
    }

    @Test
    void lineMapperMapsAllFields() throws Exception {
        TransactionData data = config.transactionLineMapper()
                .mapLine("T9,A9,2024-03-03,5.5,INR,DEBIT,hello", 4);

        assertThat(data.getTransactionId()).isEqualTo("T9");
        assertThat(data.getAccountNumber()).isEqualTo("A9");
        assertThat(data.getTransactionDate()).isEqualTo("2024-03-03");
        assertThat(data.getAmount()).isEqualTo("5.5");
        assertThat(data.getCurrency()).isEqualTo("INR");
        assertThat(data.getTransactionType()).isEqualTo("DEBIT");
        assertThat(data.getDescription()).isEqualTo("hello");
        assertThat(data.getRowNumber()).isEqualTo(4L);
    }
}