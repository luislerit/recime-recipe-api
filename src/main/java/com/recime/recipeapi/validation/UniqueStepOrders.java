package com.recime.recipeapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = UniqueStepOrdersValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueStepOrders {
    String message() default "instructions must not contain duplicate step orders";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
