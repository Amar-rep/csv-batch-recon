package com.example.batch.writer;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.batch.dto.TransactionProcessingResult;
import com.example.batch.service.TransactionWriterService;

import lombok.extern.slf4j.Slf4j;

@Service
@StepScope
@Slf4j
public class TransactionWriter implements ItemWriter<TransactionProcessingResult> {

	
	private final TransactionWriterService transactionWriterService;
	@Value("#{stepExecution.jobExecution.jobInstance.id}")
	private Long runId;

	public TransactionWriter(TransactionWriterService writerService) {
		this.transactionWriterService=writerService;
	}
	@Override
	public void write(Chunk<? extends TransactionProcessingResult> chunk) {
		transactionWriterService.write(runId, chunk.getItems());
	}

	
}
