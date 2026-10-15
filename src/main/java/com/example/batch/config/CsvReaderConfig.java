package com.example.batch.config;

import java.nio.file.Path;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.example.batch.model.UserRecord;

@Configuration
public class CsvReaderConfig {

	@Bean
	@StepScope
	public FlatFileItemReader<UserRecord> csvReader(
			@Value("${batch.file.path}") String inputFilePath) {
		Path path = Path.of(inputFilePath).toAbsolutePath().normalize();

		return new FlatFileItemReaderBuilder<UserRecord>()
				.name("userReader")
				.resource(new FileSystemResource(path))
				.delimited()
				.names("employee_id", "firstName", "lastName", "email", "salary")
				.targetType(UserRecord.class)
				.linesToSkip(1)
				.strict(true)
				.saveState(true)
				.build();
	}

}
