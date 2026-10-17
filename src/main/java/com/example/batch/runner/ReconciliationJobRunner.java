package com.example.batch.runner;

import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ReconciliationJobRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job reconciliationJob;
    private final Environment environment;

    public ReconciliationJobRunner(
            JobLauncher jobLauncher,
            @Qualifier("TransactionReconJob") Job reconciliationJob,
            Environment environment) {

        this.jobLauncher = jobLauncher;
        this.reconciliationJob = reconciliationJob;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String file1Path = environment.getProperty(
                "file1Path",
                environment.getProperty("batch.file1.path")
        );

        String file2Path = environment.getProperty(
                "file2Path",
                environment.getProperty("batch.file2.path")
        );

        if (file1Path == null || file1Path.isBlank()
                || file2Path == null || file2Path.isBlank()) {
            throw new IllegalArgumentException(
                    "Both File 1 and File 2 paths must be configured."
            );
        }

        JobParameters parameters = new JobParametersBuilder()
                .addString("file1Path", file1Path)
                .addString("file2Path", file2Path)
                .toJobParameters();

        JobExecution execution = jobLauncher.run(
                reconciliationJob,
                parameters
        );

        log.info(
                "Reconciliation job launched. Execution ID: {}, Status: {}",
                execution.getId(),
                execution.getStatus()
        );
    }
}