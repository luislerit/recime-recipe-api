package com.recime.recipeapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeRequest {

    private String title;
    private String description;
    private Integer servings;
    private Boolean isVegetarian;
    private Set<IngredientRequest> ingredients;
    private Set<InstructionRequest> instructions;


}
