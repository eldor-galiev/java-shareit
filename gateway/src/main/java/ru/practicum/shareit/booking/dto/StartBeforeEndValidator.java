package ru.practicum.shareit.booking.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StartBeforeEndValidator implements ConstraintValidator<StartBeforeEnd, BookItemRequestDto> {

	@Override
	public boolean isValid(BookItemRequestDto dto, ConstraintValidatorContext context) {
		if (dto.getStart() == null || dto.getEnd() == null) {
			return true;
		}
		return dto.getEnd().isAfter(dto.getStart());
	}
}
