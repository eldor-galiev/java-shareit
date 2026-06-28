package ru.practicum.shareit.booking.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StartBeforeEndValidator.class)
public @interface StartBeforeEnd {
	String message() default "End date must be after start date";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
