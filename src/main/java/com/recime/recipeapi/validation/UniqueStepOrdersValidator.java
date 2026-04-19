package com.recime.recipeapi.validation;

import com.recime.recipeapi.dto.request.InstructionRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;

public class UniqueStepOrdersValidator implements ConstraintValidator<UniqueStepOrders, Set<InstructionRequest>> {

    @Override
    public boolean isValid(Set<InstructionRequest> instructions, ConstraintValidatorContext context) {
        if (instructions == null || instructions.isEmpty()) return true;

        Set<Integer> seen = new HashSet<>();
        for (InstructionRequest instruction : instructions) {
            if (instruction.getStepOrder() != null && !seen.add(instruction.getStepOrder())) {
                return false;
            }
        }
        return true;
    }
}
