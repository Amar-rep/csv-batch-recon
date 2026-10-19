package com.example.batch.writer;

import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.batch.dto.ReconciliationResult;
import com.example.batch.dto.ReconciliationStatus;
import com.example.batch.service.ReconciliationWriterService;

@ExtendWith(MockitoExtension.class)
public class ReconciliationWriterTest {

    @Mock
    private ReconciliationWriterService service;

    @Test
    void delegatesChunkToServiceWithRunId() {
        ReconciliationWriter writer = new ReconciliationWriter(service);
        ReflectionTestUtils.setField(writer, "runId", 3L);
        ReconciliationResult result = new ReconciliationResult(null, ReconciliationStatus.MATCHED, null, null);
        writer.write(new Chunk<>(List.of(result)));
        verify(service).write(3L, List.of(result));
    }
}