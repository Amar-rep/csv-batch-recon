package com.example.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionProcessingResult {
	TransactionData transactionData;
	 RecordValidationStatus validationStatus;
     String errorMessage;
}
