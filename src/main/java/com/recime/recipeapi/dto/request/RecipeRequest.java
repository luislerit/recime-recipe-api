package com.recime.recipeapi.dto.request;

import com.recime.recipeapi.validation.UniqueStepOrders;
import jakarta.validation.Valid;
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
    @Valid
    private Set<IngredientRequest> ingredients;
    @Valid
    @UniqueStepOrders
    private Set<InstructionRequest> instructions;


}
