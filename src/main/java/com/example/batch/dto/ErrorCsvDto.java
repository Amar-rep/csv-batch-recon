package com.example.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ErrorCsvDto {
	private TransactionData data;
	private String status;
	private String message;

}
