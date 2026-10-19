package com.example.batch.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.batch.model.Transaction;

public class BatchWriterConfigTest {

	@Test
	void writerIsBuiltWithInsertSql() {
		JdbcBatchItemWriter<Transaction> writer = new BatchWriterConfig()
				.transactionBatchItemWriter(mock(DataSource.class));

		assertThat(writer).isNotNull();
		assertThat((String) ReflectionTestUtils.getField(writer, "sql")).contains("INSERT INTO transactions")
				.contains(":runId").contains(":transactionId");
		assertThat(ReflectionTestUtils.getField(writer, "assertUpdates")).isEqualTo(true);
	}
}