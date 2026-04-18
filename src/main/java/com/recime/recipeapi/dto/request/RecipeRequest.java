package com.recime.recipeapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(max = 50)
    private String title;
    @Size(max = 100)
    private String description;
    @Min(0)
    @Max(500)
    private Integer servings;
    private Boolean isVegetarian;
    private Set<IngredientRequest> ingredients;
    private Set<InstructionRequest> instructions;


}
