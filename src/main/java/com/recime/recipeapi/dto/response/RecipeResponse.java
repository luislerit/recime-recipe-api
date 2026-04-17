package com.recime.recipeapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeResponse {

    private Integer id;
    private UUID userId;
    private String title;
    private String description;
    private Integer servings;
    private Boolean isVegetarian;
    private Set<IngredientResponse> ingredients;
    private Set<InstructionResponse> instructions;
}
