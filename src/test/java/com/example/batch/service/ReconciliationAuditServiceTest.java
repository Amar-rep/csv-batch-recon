package com.example.batch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.batch.model.ReconciliationAudit;
import com.example.batch.repository.ReconciliationAuditRepository;

@ExtendWith(MockitoExtension.class)
public class ReconciliationAuditServiceTest {

    @Mock
    private ReconciliationAuditRepository repository;

    @Test
    void initializeSavesZeroedAudit() {
        when(repository.existsByRunId(5L)).thenReturn(false);

        new ReconciliationAuditService(repository).initialize(5L);

        ArgumentCaptor<ReconciliationAudit> captor = ArgumentCaptor.forClass(ReconciliationAudit.class);
        verify(repository).save(captor.capture());
        ReconciliationAudit audit = captor.getValue();
        assertThat(audit.getRunId()).isEqualTo(5L);
        assertThat(audit.getMatchedRecords()).isZero();
        assertThat(audit.getMismatchedRecords()).isZero();
        assertThat(audit.getInvalidRecords()).isZero();
        assertThat(audit.getDuplicateRecords()).isZero();
        assertThat(audit.getMissing_from_first()).isZero();
    }

    @Test
    void initializeSkipsWhenAuditExists() {
        when(repository.existsByRunId(5L)).thenReturn(true);

        new ReconciliationAuditService(repository).initialize(5L);

        verify(repository, never()).save(any());
    }
}