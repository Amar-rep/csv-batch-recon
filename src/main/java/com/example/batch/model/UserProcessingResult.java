package com.example.batch.model;

public record UserProcessingResult(
		UserRecord record,
		boolean valid,
		String errorMessage) {

	public static UserProcessingResult valid(UserRecord record) {
		return new UserProcessingResult(record, true, null);
	}

	public static UserProcessingResult invalid(UserRecord record, String errorMessage) {
		return new UserProcessingResult(record, false, errorMessage);
	}
}
