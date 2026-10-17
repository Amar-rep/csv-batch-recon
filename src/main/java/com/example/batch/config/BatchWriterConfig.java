package com.example.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.batch.model.Transaction;

@Configuration
public class BatchWriterConfig {

	private static final String INSERT_TRANSACTION_SQL = """
			INSERT INTO transactions
			    (run_id, transaction_id, account_number, transaction_date,
			     amount, currency, transaction_type, description,
			     row_number, created_at, updated_at)
			VALUES
			    (:runId, :transactionId, :accountNumber, :transactionDate,
			     :amount, :currency, :transactionType, :description,
			     :rowNumber, :createdAt, :updatedAt)
			""";

	@Bean
	public JdbcBatchItemWriter<Transaction> transactionBatchItemWriter(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Transaction>().dataSource(dataSource).sql(INSERT_TRANSACTION_SQL).beanMapped()
				.assertUpdates(true).build();
	}

}
