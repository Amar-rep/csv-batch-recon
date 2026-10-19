package com.example.batch.config;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import com.example.batch.service.FileValidationTasklet;

@Configuration
public class TaskletConfig {
	@Bean
	@StepScope
	public FileValidationTasklet file1ValidationTasklet(@Value("#{jobParameters['file1Path']}") String filePath,
			@Value("${csv.max-file-size}") DataSize maxFileSize) {
		return new FileValidationTasklet(filePath, maxFileSize);
	}

	@Bean
	@StepScope
	public FileValidationTasklet file2ValidationTasklet(@Value("#{jobParameters['file2Path']}") String filePath,
			@Value("${csv.max-file-size}") DataSize maxFileSize) {
		return new FileValidationTasklet(filePath, maxFileSize);
	}

}
