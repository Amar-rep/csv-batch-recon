package com.example.batch.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.batch.dto.ReconciliationResult;
import com.example.batch.dto.TransactionData;
import com.example.batch.dto.TransactionProcessingResult;
import com.example.batch.processor.ReconciliationProcessor;
import com.example.batch.processor.TransactionProcessor;
import com.example.batch.writer.TransactionWriter;

public class AsyncIntegrationConfigTest {

    private final AsyncIntegrationConfig config = new AsyncIntegrationConfig();
    private final TaskExecutor executor = mock(TaskExecutor.class);

    @Test
    void asyncTransactionProcessorIsWired() {
        TransactionProcessor delegate = new TransactionProcessor();

        AsyncItemProcessor<TransactionData, TransactionProcessingResult> processor =
                config.asyncTransactionProcessor(delegate, executor);

        assertThat(ReflectionTestUtils.getField(processor, "delegate")).isSameAs(delegate);
        assertThat(ReflectionTestUtils.getField(processor, "taskExecutor")).isSameAs(executor);
    }

    @Test
    void asyncTransactionWriterIsWired() {
        TransactionWriter delegate = mock(TransactionWriter.class);

        AsyncItemWriter<TransactionProcessingResult> writer = config.asyncTransactionWriter(delegate);

        assertThat(ReflectionTestUtils.getField(writer, "delegate")).isSameAs(delegate);
    }

    @Test
    void asyncReconciliationProcessorIsWired() {
        ReconciliationProcessor delegate = mock(ReconciliationProcessor.class);

        AsyncItemProcessor<TransactionData, ReconciliationResult> processor =
                config.asyncReconciliationProcessor(delegate, executor);

        assertThat(ReflectionTestUtils.getField(processor, "delegate")).isSameAs(delegate);
        assertThat(ReflectionTestUtils.getField(processor, "taskExecutor")).isSameAs(executor);
    }

    @Test
    void asyncReconciliationWriterIsWired() {
        ItemWriter<ReconciliationResult> delegate = mock(ItemWriter.class);

        AsyncItemWriter<ReconciliationResult> writer = config.asyncReconciliationWriter(delegate);

        assertThat(ReflectionTestUtils.getField(writer, "delegate")).isSameAs(delegate);
    }
}