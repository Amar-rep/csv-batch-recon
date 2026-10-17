package com.example.batch.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.batch.model.ReconciliationAudit;
import com.example.batch.repository.ReconciliationAuditRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReconciliationAuditService {

    private final ReconciliationAuditRepository auditRepository;

    @Transactional
    public void initialize(Long runId) {
        if (auditRepository.existsByRunId(runId)) {
            return;
        }

        ReconciliationAudit audit = new ReconciliationAudit();
        audit.setRunId(runId);
        audit.setMatchedRecords(0L);
        audit.setMismatchedRecords(0L);
        audit.setInvalidRecords(0L);
        audit.setDuplicateRecords(0L);
      

        auditRepository.save(audit);
    }
}