package com.example.batch.processor;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.example.batch.model.UserProcessingResult;
import com.example.batch.model.UserRecord;

@Component
public class UserProcessor implements ItemProcessor<UserRecord, UserProcessingResult> {

	@Override
	public UserProcessingResult process(UserRecord user) {

		if (user == null) {
			return UserProcessingResult.invalid(null, "UserRecord null error");
		}

		if (!isValidEmail(user.getEmail())) {
			return UserProcessingResult.invalid(user, "Invalid email");
		}
		if (!isValidSalary(user.getSalary())) {
			return UserProcessingResult.invalid(user, "Invalid salary info");
		}

		if (!isValidName(user.getFirstName(), user.getLastName())) {
			return UserProcessingResult.invalid(user, "Invalid name");
		}

		user.setFirstName(trimAndUppercase(user.getFirstName()));
		user.setLastName(trim(user.getLastName()));
		user.setEmail(trim(user.getEmail()).toLowerCase());
		return UserProcessingResult.valid(user);
	}

	private boolean isValidName(String firstName, String lastName) {
		return firstName != null && !firstName.isBlank()
				&& lastName != null && !lastName.isBlank();
	}

	private boolean isValidEmail(String email) {
		return email != null && EmailValidator.getInstance().isValid(email.trim());
	}

	private boolean isValidSalary(Double salary) {
		return salary != null && salary >= 0;
	}

	private String trimAndUppercase(String value) {
		String trimmed = trim(value);
		return trimmed == null ? null : trimmed.toUpperCase();
	}

	private String trim(String value) {
		return value == null ? null : value.trim();
	}
}
