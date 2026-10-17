package com.example.batch.config;

import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import com.example.batch.dto.ReconciliationResult;
import com.example.batch.dto.TransactionData;
import com.example.batch.dto.TransactionProcessingResult;
import com.example.batch.processor.ReconciliationProcessor;
import com.example.batch.processor.TransactionProcessor;
import com.example.batch.writer.TransactionWriter;

@Configuration
public class AsyncIntegrationConfig {
	@Bean
	public AsyncItemProcessor<TransactionData, TransactionProcessingResult> asyncTransactionProcessor(TransactionProcessor transactionProcessor,
			@Qualifier("batchTaskExecutor") TaskExecutor batchTaskExecutor) {
		
		AsyncItemProcessor<TransactionData, TransactionProcessingResult> processor = new AsyncItemProcessor<>();
		processor.setDelegate(transactionProcessor);
		processor.setTaskExecutor(batchTaskExecutor);
		return processor;
	}

	@Bean
	public AsyncItemWriter<TransactionProcessingResult> asyncTransactionWriter( TransactionWriter transactionWriter) {
		AsyncItemWriter<TransactionProcessingResult> writer = new AsyncItemWriter<>();
		writer.setDelegate(transactionWriter);
		return writer;
	}
	@Bean
	public AsyncItemProcessor<TransactionData, ReconciliationResult>
	        asyncReconciliationProcessor(
	                ReconciliationProcessor reconciliationProcessor,
	                @Qualifier("batchTaskExecutor")
	                TaskExecutor batchTaskExecutor) {

	    AsyncItemProcessor<TransactionData, ReconciliationResult> processor =
	            new AsyncItemProcessor<>();

	    processor.setDelegate(reconciliationProcessor);
	    processor.setTaskExecutor(batchTaskExecutor);

	    return processor;
	}
	@Bean
	public AsyncItemWriter<ReconciliationResult> asyncReconciliationWriter(
	        @Qualifier("reconciliationWriter")
	        ItemWriter<ReconciliationResult> reconciliationWriter) {

	    AsyncItemWriter<ReconciliationResult> writer = new AsyncItemWriter<>();
	    writer.setDelegate(reconciliationWriter);
	    return writer;
	}

}
