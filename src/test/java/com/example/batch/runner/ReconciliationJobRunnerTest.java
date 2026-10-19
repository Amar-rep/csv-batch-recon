package com.example.batch.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(MockitoExtension.class)
public class ReconciliationJobRunnerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job job;

    @Mock
    private JobExecution execution;

    private final MockEnvironment environment = new MockEnvironment();
    private final ApplicationArguments args = new DefaultApplicationArguments();

    private ReconciliationJobRunner runner() {
        return new ReconciliationJobRunner(jobLauncher, job, environment);
    }

    @Test
    void launchesJobWithConfiguredPaths() throws Exception {
        environment.setProperty("batch.file1.path", "/data/one.csv");
        environment.setProperty("batch.file2.path", "/data/two.csv");
        when(jobLauncher.run(eq(job), any(JobParameters.class))).thenReturn(execution);

        runner().run(args);

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(job), captor.capture());
        assertThat(captor.getValue().getString("file1Path")).isEqualTo("/data/one.csv");
        assertThat(captor.getValue().getString("file2Path")).isEqualTo("/data/two.csv");
        assertThat(captor.getValue().getParameters()).hasSize(2);
    }

  
    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
            "NULL, /data/two.csv",
            "'  ', /data/two.csv",
            "/data/one.csv, NULL",
            "/data/one.csv, '  '",
            "NULL, NULL" })
    void failsWhenAPathIsMissing(String file1, String file2) {
        if (file1 != null) {
            environment.setProperty("file1Path", file1);
        }
        if (file2 != null) {
            environment.setProperty("file2Path", file2);
        }

        assertThatThrownBy(() -> runner().run(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Both File 1 and File 2 paths must be configured.");
        verifyNoInteractions(jobLauncher);
    }
}