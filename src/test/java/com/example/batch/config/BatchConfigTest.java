package com.example.batch.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.batch.listener.ReconciliationJobListener;
import com.example.batch.service.FileValidationTasklet;

@SuppressWarnings("unchecked")
public class BatchConfigTest {

	private final BatchConfig config = new BatchConfig();
	private final JobRepository jobRepository = mock(JobRepository.class);
	private final PlatformTransactionManager txManager = mock(PlatformTransactionManager.class);

	private Step validateFile1() {
		return config.validateInputFileStep(jobRepository, txManager, mock(FileValidationTasklet.class));
	}

	private Step validateFile2() {
		return config.validateFile2Step(jobRepository, txManager, mock(FileValidationTasklet.class));
	}

	private Step importStep() {
		return config.importTransactionsStep(jobRepository, txManager, mock(FlatFileItemReader.class),
				mock(AsyncItemProcessor.class), mock(AsyncItemWriter.class));
	}

	private Step reconcileStep() {
		return config.reconcileTransactionsStep(jobRepository, txManager, mock(FlatFileItemReader.class),
				mock(AsyncItemProcessor.class), mock(AsyncItemWriter.class));
	}

	@Test
	void validateInputFileStepIsRestartable() {
		Step step = validateFile1();

		assertThat(step.getName()).isEqualTo("validateInputFileStep");
		assertThat(step.isAllowStartIfComplete()).isTrue();
		assertThat(step).isInstanceOf(TaskletStep.class);
	}

	@Test
	void validateFile2StepIsRestartable() {
		Step step = validateFile2();

		assertThat(step.getName()).isEqualTo("validateFile2Step");
		assertThat(step.isAllowStartIfComplete()).isTrue();
	}

	@Test
	void importTransactionsStepIsBuilt() {
		Step step = importStep();

		assertThat(step.getName()).isEqualTo("importTransactionsStep");
		assertThat(step).isInstanceOf(TaskletStep.class);
		assertThat(step.isAllowStartIfComplete()).isFalse();
	}

	@Test
	void reconcileTransactionsStepIsBuilt() {
		Step step = reconcileStep();

		assertThat(step.getName()).isEqualTo("reconcileTransactionsStep");
		assertThat(step).isInstanceOf(TaskletStep.class);
	}

	@Test
	void jobContainsAllSteps() {
		Job job = config.TransactionReconJob(jobRepository, mock(ReconciliationJobListener.class), validateFile1(),
				importStep(), validateFile2(), reconcileStep());

		assertThat(job.getName()).isEqualTo("TransactionReconJob");
		assertThat(((StepLocator) job).getStepNames()).contains("validateInputFileStep", "importTransactionsStep",
				"validateFile2Step", "reconcileTransactionsStep");
	}
}