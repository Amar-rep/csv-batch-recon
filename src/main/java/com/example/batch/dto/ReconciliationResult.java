package com.example.batch.dto;

import com.example.batch.model.Transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationResult {
	private TransactionData originalRecord;
	private ReconciliationStatus status;
	private String reason;
	private Transaction file1Transaction;
}
