package com.example.batch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.unit.DataSize;

@ExtendWith(MockitoExtension.class)
public class FileValidationTaskletTest {

    private static final String HEADER =
            "transaction_id,account_number,transaction_date,amount,currency,transaction_type,description";
    private static final DataSize BIG = DataSize.ofMegabytes(1);

    @TempDir
    Path dir;

    @Mock
    private StepContribution contribution;

    private Path file(String name, String content) throws IOException {
        Path path = dir.resolve(name);
        Files.writeString(path, content);
        return path;
    }

    private RepeatStatus run(String path, DataSize max) throws Exception {
        return new FileValidationTasklet(path, max).execute(contribution, null);
    }

    private ExitStatus failure(String path, DataSize max) throws Exception {
        assertThat(run(path, max)).isEqualTo(RepeatStatus.FINISHED);
        ArgumentCaptor<ExitStatus> captor = ArgumentCaptor.forClass(ExitStatus.class);
        verify(contribution).setExitStatus(captor.capture());
        return captor.getValue();
    }

    @Test
    void validFileProducesNoFailure() throws Exception {
        Path path = file("ok.csv", HEADER + "\nT1,A1,2024-01-01,1,USD,CREDIT,d\n");

        assertThat(run(path.toString(), BIG)).isEqualTo(RepeatStatus.FINISHED);

        verify(contribution, never()).setExitStatus(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "  ")
    void missingPath(String path) throws Exception {
        ExitStatus status = failure(path, BIG);

        assertThat(status.getExitCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(status.getExitDescription()).isEqualTo("File path missing");
    }

    @Test
    void nonExistingFile() throws Exception {
        Path path = dir.resolve("nope.csv");

        ExitStatus status = failure(path.toString(), BIG);

        assertThat(status.getExitCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(status.getExitDescription())
                .isEqualTo("File does not exist: " + path.toAbsolutePath().normalize());
    }

    @Test
    void notACsvFile() throws Exception {
        Path path = file("data.txt", HEADER + "\nrow\n");

        ExitStatus status = failure(path.toString(), BIG);

        assertThat(status.getExitDescription()).isEqualTo("Not a CSV file");
    }

 

    @Test
    void emptyFile() throws Exception {
        Path path = file("empty.csv", "");

        ExitStatus status = failure(path.toString(), BIG);

        assertThat(status.getExitDescription()).isEqualTo("File is empty,Invalid CSV headers");
    }

    @Test
    void invalidHeader() throws Exception {
        Path path = file("bad.csv", "wrong,header\n1,2\n");

        ExitStatus status = failure(path.toString(), BIG);

        assertThat(status.getExitDescription()).isEqualTo("Invalid CSV headers");
    }

   

    @ParameterizedTest
    @CsvSource({ "0, true", "1, false" })
    void fileSizeBoundary(long extraBytes, boolean expectError) throws Exception {
        Path path = file("size.csv", HEADER + "\nT1,A1,2024-01-01,1,USD,CREDIT,d\n");
        long size = Files.size(path);

        run(path.toString(), DataSize.ofBytes(size + extraBytes));

        if (expectError) {
            ArgumentCaptor<ExitStatus> captor = ArgumentCaptor.forClass(ExitStatus.class);
            verify(contribution).setExitStatus(captor.capture());
            assertThat(captor.getValue().getExitDescription()).isEqualTo("Invalid file size too large");
        } else {
            verify(contribution, never()).setExitStatus(any());
        }
    }
}