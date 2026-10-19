package com.example.batch.writer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.batch.dto.ErrorCsvDto;
import com.example.batch.dto.ReconciliationResult;
import com.example.batch.dto.ReconciliationStatus;
import com.example.batch.dto.RecordValidationStatus;
import com.example.batch.dto.TransactionData;
import com.example.batch.dto.TransactionProcessingResult;

public class TransactionErrorCsvWriterTest {

    private static final String HEADER = "row_number,run_id,job_executionId,transaction_id,account_number,"
            + "transaction_date,amount,currency,status,error_message";

    @TempDir
    Path tempDir;

    private Path outputDir;
    private Path outputFile;
    private TransactionErrorCsvWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        outputDir = tempDir.resolve("out/errors");
        outputFile = outputDir.resolve("transaction-errors-7.csv");
        writer = new TransactionErrorCsvWriter(outputDir.toString(), 7L);
        ReflectionTestUtils.setField(writer, "jobExecutionId", 99L);
    }

    private TransactionData data() {
        return new TransactionData(2L, "T1", "ACC1", "2024-01-01", "10.5", "USD", "CREDIT", "d");
    }

    private List<String> lines() throws IOException {
        return Files.readAllLines(outputFile);
    }

    @Test
    void constructorCreatesDirectoryButNotFile() {
        assertThat(outputDir).isDirectory();
        assertThat(outputFile).doesNotExist();
    }

    @Test
    void writesHeaderAndTransactionErrorRow() throws IOException {
        writer.writeTransactionErrors(
                List.of(new TransactionProcessingResult(data(), RecordValidationStatus.INVALID, "Invalid currency")),
                7L);

        assertThat(lines()).containsExactly(HEADER,
                "2,7,99,T1,ACC1,2024-01-01,10.5,USD,INVALID,Invalid currency");
    }

    @Test
    void secondWriteAppendsWithoutHeader() throws IOException {
        TransactionProcessingResult result =
                new TransactionProcessingResult(data(), RecordValidationStatus.INVALID, "bad");

        writer.writeTransactionErrors(List.of(result), 7L);
        writer.writeTransactionErrors(List.of(result), 7L);

        assertThat(lines()).hasSize(3);
        assertThat(lines()).filteredOn(HEADER::equals).hasSize(1);
    }

    @Test
    void writesHeaderWhenExistingFileIsEmpty() throws IOException {
        Files.createFile(outputFile);

        writer.writeTransactionErrors(
                List.of(new TransactionProcessingResult(data(), RecordValidationStatus.INVALID, "bad")), 7L);

        assertThat(lines().get(0)).isEqualTo(HEADER);
        assertThat(lines()).hasSize(2);
    }

    @Test
    void emptyListWritesNothing() throws IOException {
        writer.write(List.of(), 7L);

        assertThat(outputFile).doesNotExist();
    }

    @Test
    void writesReconciliationErrorRow() throws IOException {
        writer.writeReconciliationErrors(List.of(new ReconciliationResult(data(),
                ReconciliationStatus.MISSING, "Transaction not found in File 1", null)), 7L);

        assertThat(lines()).containsExactly(HEADER,
                "2,7,99,T1,ACC1,2024-01-01,10.5,USD,MISSING,Transaction not found in File 1");
    }

    @Test
    void nullDataIsWrittenAsEmptyColumns() throws IOException {
        ErrorCsvDto dto = new ErrorCsvDto();
        dto.setStatus("INVALID");
        dto.setMessage("msg");

        writer.write(List.of(dto), 7L);

        assertThat(lines().get(1)).endsWith(",7,99,,,,,,INVALID,msg");
    }

    @Test
    void nullRunIdIsWrittenAsEmptyColumn() throws IOException {
        ErrorCsvDto dto = new ErrorCsvDto();
        dto.setData(data());
        dto.setStatus("INVALID");
        dto.setMessage("msg");

        writer.write(List.of(dto), null);

        assertThat(lines().get(1)).isEqualTo("2,,99,T1,ACC1,2024-01-01,10.5,USD,INVALID,msg");
    }

    @ParameterizedTest
    @EnumSource(RecordValidationStatus.class)
    void statusNameIsWritten(RecordValidationStatus status) throws IOException {
        writer.writeTransactionErrors(List.of(new TransactionProcessingResult(data(), status, "msg")), 7L);

        assertThat(lines().get(1)).endsWith(",99,T1,ACC1,2024-01-01,10.5,USD," + status.name() + ",msg");
    }
}