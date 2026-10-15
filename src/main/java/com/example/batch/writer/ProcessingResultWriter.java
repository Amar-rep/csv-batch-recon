package com.example.batch.writer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.batch.model.FailedUserRecord;
import com.example.batch.model.UserProcessingResult;
import com.example.batch.model.UserRecord;

@Component
@StepScope
public class ProcessingResultWriter implements ItemWriter<UserProcessingResult> {

	private final JdbcBatchItemWriter<UserRecord> userWriter;
	private final JdbcBatchItemWriter<FailedUserRecord> failedUserWriter;
	private final Long jobExecutionId;

	public ProcessingResultWriter(
			@Qualifier("userWriter") JdbcBatchItemWriter<UserRecord> userWriter,
			@Qualifier("failedUserWriter") JdbcBatchItemWriter<FailedUserRecord> failedUserWriter,
			@Value("#{stepExecution.jobExecution.id}") Long jobExecutionId) {
		this.userWriter = userWriter;
		this.failedUserWriter = failedUserWriter;
		this.jobExecutionId = jobExecutionId;
	}

	@Override
	public void write(Chunk<? extends UserProcessingResult> chunk) throws Exception {
		List<UserRecord> validRecords = new ArrayList<>();
		List<FailedUserRecord> failedRecords = new ArrayList<>();

		for (UserProcessingResult result : chunk) {
			if (result.valid()) {
				validRecords.add(result.record());
			} else {
				failedRecords.add(toFailedRecord(result));
			}
		}

		if (!validRecords.isEmpty()) {
			userWriter.write(new Chunk<>(validRecords));
		}

		if (!failedRecords.isEmpty()) {
			failedUserWriter.write(new Chunk<>(failedRecords));
		}
	}

	private FailedUserRecord toFailedRecord(UserProcessingResult result) {
		UserRecord source = result.record();
		FailedUserRecord failed = new FailedUserRecord();
		failed.setJobExecutionId(jobExecutionId);
		failed.setCreatedAt(LocalDateTime.now());
		failed.setErrorMessage(result.errorMessage());

		if (source != null) {
			failed.setFirstName(source.getFirstName());
			failed.setLastName(source.getLastName());
			failed.setEmail(source.getEmail());
		}

		return failed;
	}
}
