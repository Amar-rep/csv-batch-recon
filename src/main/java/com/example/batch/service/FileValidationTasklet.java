package com.example.batch.service;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.unit.DataSize;

public class FileValidationTasklet implements Tasklet {

	private static final String HEADER = "transaction_id,account_number,transaction_date,amount,currency,transaction_type,description";

	private String filePath;

	private DataSize maxFileSize;

	public FileValidationTasklet(String filePath, DataSize maxFileSize) {
		this.filePath = filePath;
		this.maxFileSize = maxFileSize;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		List<String> errorList = new ArrayList<>();

		if (filePath == null || filePath.isBlank()) {
			errorList.add("File path missing");
		} else {
			Path path = Path.of(filePath).toAbsolutePath().normalize();
			if (!Files.exists(path)) {
				errorList.add("File does not exist: " + path);
			} else {
				if (!path.toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {

					errorList.add("Not a CSV file");
				}
				long fileSize = Files.size(path);
				if (fileSize == 0) {

					errorList.add("File is empty");
				}
				if (fileSize >= maxFileSize.toBytes()) {
					errorList.add("Invalid file size too large");
				}
				try (BufferedReader reader = Files.newBufferedReader(path)) {
					String header = reader.readLine();

					if (!HEADER.equals(header)) {
						errorList.add("Invalid CSV headers");
					}
				}
			}
		}
		if (!errorList.isEmpty()) {
			contribution.setExitStatus(new ExitStatus("VALIDATION_FAILED", String.join(",", errorList)));
		}
		return RepeatStatus.FINISHED;
	}
}
