package com.example.batch.listener;


import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import com.example.batch.service.ReconciliationAuditService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReconciliationJobListener implements JobExecutionListener {

    private final ReconciliationAuditService auditService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        Long runId = jobExecution.getJobInstance().getInstanceId();

        auditService.initialize(runId);
    }
}