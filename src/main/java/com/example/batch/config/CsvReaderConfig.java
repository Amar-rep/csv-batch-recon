package com.example.batch.config;

import java.nio.file.Path;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.example.batch.dto.TransactionData;

@Configuration
public class CsvReaderConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<TransactionData> file1Reader(
            @Value("${batch.file1.path}") String inputFilePath,
            LineMapper<TransactionData> transactionLineMapper) {
    	//	throw new RuntimeException();
        return createReader(inputFilePath, transactionLineMapper, "file1Reader");
    }

    @Bean
    @StepScope
    public FlatFileItemReader<TransactionData> file2Reader(
            @Value("${batch.file2.path}") String inputFilePath,
            LineMapper<TransactionData> transactionLineMapper) {

        return createReader(inputFilePath, transactionLineMapper, "file2Reader");
    }

    private FlatFileItemReader<TransactionData> createReader(
            String inputFilePath,
            LineMapper<TransactionData> transactionLineMapper,
            String readerName) {

        Path path = Path.of(inputFilePath)
                .toAbsolutePath()
                .normalize();

        return new FlatFileItemReaderBuilder<TransactionData>()
                .name(readerName)
                .resource(new FileSystemResource(path))
                .lineMapper(transactionLineMapper)
                .linesToSkip(1)
                .strict(true)
                .saveState(true)
                .build();
    }

    @Bean
    public LineMapper<TransactionData> transactionLineMapper() {

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(
                "transactionId",
                "accountNumber",
                "transactionDate",
                "amount",
                "currency",
                "transactionType",
                "description"
        );

        DefaultLineMapper<TransactionData> delegate = new DefaultLineMapper<>();
        delegate.setLineTokenizer(tokenizer);

        delegate.setFieldSetMapper(fieldSet -> {
            TransactionData data = new TransactionData();
            data.setTransactionId(fieldSet.readString("transactionId"));
            data.setAccountNumber(fieldSet.readString("accountNumber"));
            data.setTransactionDate(fieldSet.readString("transactionDate"));
            data.setAmount(fieldSet.readString("amount"));
            data.setCurrency(fieldSet.readString("currency"));
            data.setTransactionType(fieldSet.readString("transactionType"));
            data.setDescription(fieldSet.readString("description"));
            return data;
        });

        return (line, lineNumber) -> {
            TransactionData data = delegate.mapLine(line, lineNumber);
            data.setRowNumber(lineNumber);
            return data;
        };
    }
}
