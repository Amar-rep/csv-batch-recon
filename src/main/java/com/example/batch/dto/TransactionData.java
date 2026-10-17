package com.example.batch.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionData {
	  private long rowNumber;
	    private String transactionId;
	    private String accountNumber;
	    private String transactionDate;
	    private String amount;
	    private String currency;
	    private String transactionType;
	    private String description;
}
