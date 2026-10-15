package com.example.batch.config;

import java.util.concurrent.Future;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.batch.model.UserProcessingResult;
import com.example.batch.model.UserRecord;
import com.example.batch.service.FileValidationTasklet;

@Configuration
public class BatchConfig {

	@Bean
	public Step validateFileStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			FileValidationTasklet fileValidationTasklet) {
		return new StepBuilder("validateFileStep", jobRepository)
				.tasklet(fileValidationTasklet, transactionManager)
				.allowStartIfComplete(true)
				.build();
	}

	@Bean
	public Step importUserStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			@Qualifier("csvReader") FlatFileItemReader<UserRecord> csvReader,
			@Qualifier("asyncUserProcessor") AsyncItemProcessor<UserRecord, UserProcessingResult> asyncUserProcessor,
			@Qualifier("asyncUserWriter") AsyncItemWriter<UserProcessingResult> asyncUserWriter) {
		return new StepBuilder("importUserStep", jobRepository)
				.<UserRecord, Future<UserProcessingResult>>chunk(10,transactionManager)
				.reader(csvReader)
				.processor(asyncUserProcessor)
				.writer(asyncUserWriter)
				.build();
		
	}

	@Bean
	public Job importUserJob(JobRepository jobRepository,
			@Qualifier("validateFileStep") Step validateFileStep,
			@Qualifier("importUserStep") Step importUserStep) {
		return new JobBuilder("importUserJob", jobRepository)
				.start(validateFileStep)
				.on("VALIDATION_FAILED").fail()
				.from(validateFileStep)
				.on("COMPLETED").to(importUserStep)
				.end()
				.build();
	}

}
