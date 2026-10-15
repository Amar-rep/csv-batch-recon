package com.example.batch.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Table(name = "failed_user_record")
@Setter
@NoArgsConstructor
public class FailedUserRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "job_execution_id")
	private Long jobExecutionId;

	@Column(name = "first_name", length = 100)
	private String firstName;

	@Column(name = "last_name", length = 100)
	private String lastName;

	@Column(length = 320)
	private String email;
	
	@Column(name = "error_message", length = 1000)
	private String errorMessage;

	@Column(name = "created_at")
	private LocalDateTime createdAt;
}
