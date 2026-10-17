package com.example.batch.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transactions", uniqueConstraints = @UniqueConstraint(name = "uk_transactions_run_transaction", columnNames = {
		"run_id", "transaction_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "run_id", nullable = false)
	private Long runId;

	@Column(name = "transaction_id", nullable = false)
	private String transactionId;

	@Column(name = "account_number", nullable = false)
	private String accountNumber;

	@Column(name = "transaction_date", nullable = false)
	private LocalDate transactionDate;

	@Column(name = "amount", precision = 19, scale = 2, nullable = false)
	private BigDecimal amount;

	@Column(name = "currency", nullable = false)
	private String currency;

	@Column(name = "transaction_type", nullable = false)
	private String transactionType;

	@Column(name = "description")
	private String description;

	@Column(name = "row_number", nullable = false)
	private Long rowNumber;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}