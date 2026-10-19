package com.example.batch.listener;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;

import com.example.batch.service.ReconciliationAuditService;

@ExtendWith(MockitoExtension.class)
public class ReconciliationJobListenerTest {

    @Mock
    private ReconciliationAuditService auditService;

    @Test
    void beforeJobInitializesAuditWithInstanceId() {
        JobInstance instance = new JobInstance(7L, "TransactionReconJob");
        JobExecution execution = new JobExecution(instance, new JobParameters());

        new ReconciliationJobListener(auditService).beforeJob(execution);

        verify(auditService).initialize(7L);
    }
}
