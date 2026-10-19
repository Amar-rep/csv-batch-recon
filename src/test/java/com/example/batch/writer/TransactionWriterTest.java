package com.example.batch.writer;

import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.batch.dto.RecordValidationStatus;
import com.example.batch.dto.TransactionProcessingResult;
import com.example.batch.service.TransactionWriterService;

@ExtendWith(MockitoExtension.class)
public class TransactionWriterTest {

    @Mock
    private TransactionWriterService service;

    @Test
    void delegatesChunkToServiceWithRunId() {
        TransactionWriter writer = new TransactionWriter(service);
        ReflectionTestUtils.setField(writer, "runId", 4L);
        TransactionProcessingResult result =
                new TransactionProcessingResult(null, RecordValidationStatus.VALID, null);

        writer.write(new Chunk<>(List.of(result)));

        verify(service).write(4L, List.of(result));
    }
}