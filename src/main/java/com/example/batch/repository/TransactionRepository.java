package com.example.batch.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.batch.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{
	  @Query("""
		        SELECT t.transactionId
		        FROM Transaction t
		        WHERE t.runId = :runId
		          AND t.transactionId IN :transactionIds
		        """)
		    Set<String> findExistingTransactionIds(
		            @Param("runId") Long runId,
		            @Param("transactionIds") Set<String> transactionIds
		    );

	  Optional<Transaction> findByRunIdAndTransactionId(Long runId, String transactionId);
}
