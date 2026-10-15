package com.example.batch.config;

import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import com.example.batch.model.UserProcessingResult;
import com.example.batch.model.UserRecord;
import com.example.batch.processor.UserProcessor;
import com.example.batch.writer.ProcessingResultWriter;

@Configuration
public class AsyncIntegrationConfig {
	@Bean
	public AsyncItemProcessor<UserRecord, UserProcessingResult> asyncUserProcessor(UserProcessor userProcessor,
			@Qualifier("batchTaskExecutor") TaskExecutor batchTaskExecutor) {
		AsyncItemProcessor<UserRecord, UserProcessingResult> processor = new AsyncItemProcessor<>();
		processor.setDelegate(userProcessor);
		processor.setTaskExecutor(batchTaskExecutor);
		return processor;
	}

	@Bean
	public AsyncItemWriter<UserProcessingResult> asyncUserWriter(ProcessingResultWriter processingResultWriter) {
		AsyncItemWriter<UserProcessingResult> writer = new AsyncItemWriter<>();
		writer.setDelegate(processingResultWriter);
		return writer;
	}

}
