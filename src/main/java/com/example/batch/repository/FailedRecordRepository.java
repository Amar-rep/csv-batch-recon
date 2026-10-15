package com.example.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.batch.model.FailedUserRecord;

public interface FailedRecordRepository extends JpaRepository<FailedUserRecord,Long> {

}
