package com.example.batch.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.batch.dto.ErrorCsvDto;
import com.example.batch.dto.ReconciliationResult;
import com.example.batch.dto.RecordValidationStatus;
import com.example.batch.dto.TransactionData;
import com.example.batch.dto.TransactionProcessingResult;

@Component
@StepScope
public class TransactionErrorCsvWriter {

	private final Path outputPath;
	@Value("#{stepExecution.jobExecution.id}")
	private Long jobExecutionId;

	public TransactionErrorCsvWriter(@Value("${reconciliation.error-directory:output/errors}") String outputDirectory,
			@Value("#{stepExecution.jobExecution.jobInstance.id}") Long runId) throws IOException {
		Path directory = Path.of(outputDirectory).toAbsolutePath().normalize();

		Files.createDirectories(directory);
		this.outputPath = directory.resolve("transaction-errors-" + runId + ".csv");
	}

	public synchronized void write(List<ErrorCsvDto> records, Long runId) throws IOException {

		if (records.isEmpty()) {
			return;
		}

		Files.createDirectories(outputPath.getParent());

		boolean writeHeader = !Files.exists(outputPath) || Files.size(outputPath) == 0;

		try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE,
				StandardOpenOption.APPEND); CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

			if (writeHeader) {
				printer.printRecord("row_number", "run_id", "job_executionId", "transaction_id", "account_number",
						"transaction_date", "amount", "currency", "status", "error_message");
			}

			for (ErrorCsvDto result : records) {
				TransactionData data = result.getData();

				printer.printRecord(data == null ? null : data.getRowNumber(), runId == null ? null : runId,
						jobExecutionId, data == null ? null : data.getTransactionId(),
						data == null ? null : data.getAccountNumber(), data == null ? null : data.getTransactionDate(),
						data == null ? null : data.getAmount(), data == null ? null : data.getCurrency(),
						result.getStatus(), result.getMessage());
			}
		}
	}

	public synchronized void writeReconciliationErrors(List<ReconciliationResult> records, Long runId)
			throws IOException {
		List<ErrorCsvDto> results = new ArrayList<>();

		for (ReconciliationResult record : records) {
			ErrorCsvDto result = new ErrorCsvDto();
			result.setMessage(record.getReason());
			result.setData(record.getOriginalRecord());
			result.setStatus(record.getStatus().name());
			results.add(result);

		}
		write(results, runId);

	}

	public synchronized void writeTransactionErrors(List<TransactionProcessingResult> records, Long runId)
			throws IOException {
		List<ErrorCsvDto> results = new ArrayList<>();
		for (TransactionProcessingResult record : records) {
			ErrorCsvDto result = new ErrorCsvDto();
			result.setMessage(record.getErrorMessage());
			result.setData(record.getTransactionData());
			result.setStatus(record.getValidationStatus().name());
			results.add(result);

		}
		write(results, runId);

	}

}