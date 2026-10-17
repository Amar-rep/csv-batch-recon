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

import com.example.batch.dto.ReconciliationResult;
import com.example.batch.dto.TransactionData;
import com.example.batch.dto.TransactionProcessingResult;
import com.example.batch.listener.ReconciliationJobListener;
import com.example.batch.service.FileValidationTasklet;

@Configuration
public class BatchConfig {

	@Bean
	public Step validateInputFileStep(
	        JobRepository jobRepository,
	        PlatformTransactionManager transactionManager,
	        @Qualifier("file1ValidationTasklet")
	        FileValidationTasklet tasklet) {

	    return new StepBuilder("validateInputFileStep", jobRepository)
	            .tasklet(tasklet, transactionManager)
	            .allowStartIfComplete(true)
	            .build();
	} 
	  @Bean
	  public Step validateFile2Step(
	          JobRepository jobRepository,
	          PlatformTransactionManager transactionManager,
	          @Qualifier("file2ValidationTasklet")
	          FileValidationTasklet tasklet) {

	      return new StepBuilder("validateFile2Step", jobRepository)
	              .tasklet(tasklet, transactionManager)
	              .allowStartIfComplete(true)
	              .build();
	  }
	
	@Bean
	public Step importTransactionsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			@Qualifier("file1Reader") FlatFileItemReader<TransactionData> csvReader,
			@Qualifier("asyncTransactionProcessor") AsyncItemProcessor<TransactionData, TransactionProcessingResult> asyncTransactionProcessor,
			@Qualifier("asyncTransactionWriter") AsyncItemWriter<TransactionProcessingResult> asyncTransactionWriter) {
		return new StepBuilder("importTransactionsStep", jobRepository)
				.<TransactionData, Future<TransactionProcessingResult>>chunk(1000,transactionManager)
				.reader(csvReader)
				.processor(asyncTransactionProcessor)
				.writer(asyncTransactionWriter)
				.build();
		
	}
	
	@Bean
	public Step reconcileTransactionsStep(
	        JobRepository jobRepository,
	        PlatformTransactionManager transactionManager,
	        @Qualifier("file2Reader")
	        FlatFileItemReader<TransactionData> csvReader,
	        @Qualifier("asyncReconciliationProcessor")
	        AsyncItemProcessor<TransactionData, ReconciliationResult> asyncReconciliationProcessor,
	        @Qualifier("asyncReconciliationWriter")
	        AsyncItemWriter<ReconciliationResult> asyncReconciliationWriter) {

	    return new StepBuilder("reconcileTransactionsStep", jobRepository)
	            .<TransactionData, Future<ReconciliationResult>>chunk(
	                    1000, transactionManager)
	            .reader(csvReader)
	            .processor(asyncReconciliationProcessor)
	            .writer(asyncReconciliationWriter)
	            .build();
	}

	@Bean
	public Job TransactionReconJob(
	        JobRepository jobRepository,
	        ReconciliationJobListener listener,
	        @Qualifier("validateInputFileStep") Step validateInputFileStep,
	        @Qualifier("importTransactionsStep") Step importTransactionsStep,
	        @Qualifier("validateFile2Step") Step validateFile2Step,
	        @Qualifier("reconcileTransactionsStep") Step reconcileTransactionsStep) {

	    return new JobBuilder("TransactionReconJob", jobRepository)
	    		.listener(listener)
	            .start(validateInputFileStep)
	                .on("VALIDATION_FAILED").fail()
	            .from(validateInputFileStep)
	                .on("COMPLETED").to(importTransactionsStep)
	            .from(importTransactionsStep)
	                .on("COMPLETED").to(validateFile2Step)
	            .from(validateFile2Step)
	                .on("VALIDATION_FAILED").fail()
	            .from(validateFile2Step)
	                .on("COMPLETED").to(reconcileTransactionsStep)
	            .end()
	            .build();
	}

}
