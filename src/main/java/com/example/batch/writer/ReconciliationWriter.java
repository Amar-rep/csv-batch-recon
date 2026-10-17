package com.example.batch.writer;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.batch.dto.ReconciliationResult;
import com.example.batch.service.ReconciliationWriterService;

import lombok.RequiredArgsConstructor;

@Component("reconciliationWriter")
@StepScope
@RequiredArgsConstructor
public class ReconciliationWriter implements ItemWriter<ReconciliationResult> {
		private final ReconciliationWriterService reconciliationWriterService;
		
		@Value("#{stepExecution.jobExecution.jobInstance.id}")
		private Long runId;
		@Override
		public void write(Chunk<? extends ReconciliationResult> chunk) {
			reconciliationWriterService.write(runId, chunk.getItems());
		}
	
}
