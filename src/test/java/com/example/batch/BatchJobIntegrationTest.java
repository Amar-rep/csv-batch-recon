package com.example.batch;

import com.example.batch.repository.UserRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
class BatchJobIntegrationTest {

    @TempDir
    Path tempDir;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private UserRecordRepository userRecordRepository;

    @Test
    void importsValidRowsAndFiltersInvalidRows() throws Exception {
        userRecordRepository.deleteAll();

        Path inputFile = tempDir.resolve("users.csv");
        Files.writeString(inputFile, """
                firstName,lastName,email,salary
                Alice,Smith,alice@example.com,50000
                Invalid,Email,not-an-email,40000
                Bob,Jones,bob@example.com,60000
                Invalid,Salary,valid@example.com,-1
                Evan,Brown,evan@example.com,70000
                """);
        JobExecution execution = jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addString("inputFile", inputFile.toString())
                .addString("launchId", "integration-test")
                .toJobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        StepExecution step = execution.getStepExecutions().stream()
                .filter(stepExecution -> stepExecution.getStepName().equals("importUserStep"))
                .findFirst()
                .orElseThrow();
        assertThat(step.getReadCount()).isEqualTo(5);
        assertThat(step.getFilterCount()).isEqualTo(2);
        assertThat(step.getWriteCount()).isEqualTo(3);
        assertThat(userRecordRepository.count()).isEqualTo(3);
        assertThat(userRecordRepository.findAll())
                .extracting("firstName")
                .containsExactlyInAnyOrder("ALICE", "BOB", "EVAN");
    }

    @Test
    void failsBeforeImportWhenCsvHeaderIsInvalid() throws Exception {
        Path inputFile = tempDir.resolve("invalid-header.csv");
        Files.writeString(inputFile, "name,email\nAlice,alice@example.com\n");

        JobExecution execution = jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addString("inputFile", inputFile.toString())
                .addString("launchId", "invalid-header-test")
                .toJobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(execution.getStepExecutions())
                .hasSize(1)
                .first()
                .extracting(StepExecution::getExitStatus)
                .extracting(ExitStatus::getExitCode, ExitStatus::getExitDescription)
                .containsExactly("VALIDATION_FAILED", "Invalid CSV headers");
    }
}
