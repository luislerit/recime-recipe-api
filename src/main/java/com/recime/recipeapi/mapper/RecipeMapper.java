package com.recime.recipeapi.mapper;

import com.recime.recipeapi.dto.request.IngredientRequest;
import com.recime.recipeapi.dto.request.InstructionRequest;
import com.recime.recipeapi.dto.request.RecipeRequest;
import com.recime.recipeapi.dto.response.IngredientResponse;
import com.recime.recipeapi.dto.response.InstructionResponse;
import com.recime.recipeapi.dto.response.RecipeResponse;
import com.recime.recipeapi.entity.Ingredient;
import com.recime.recipeapi.entity.Instruction;
import com.recime.recipeapi.entity.Recipe;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RecipeMapper {

    public Recipe toEntity(RecipeRequest request, UUID userId) {
        Recipe recipe = Recipe.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .servings(resolveServings(request.getServings(), request.getIngredients()))
                .isVegetarian(request.getIsVegetarian() != null ? request.getIsVegetarian() : false)
                .build();

        recipe.setIngredients(mapIngredients(request.getIngredients(), recipe));
        recipe.setInstructions(mapInstructions(request.getInstructions(), recipe));

        return recipe;
    }

    private int resolveServings(Integer servings, Set<IngredientRequest> ingredients) {
        if (servings != null) return servings;
        return (ingredients != null && !ingredients.isEmpty()) ? 1 : 0;
    }

    public void updateEntity(Recipe recipe, RecipeRequest recipeRequest) {
        recipe.setTitle(recipeRequest.getTitle());
        recipe.setDescription(recipeRequest.getDescription());
        recipe.setServings(recipeRequest.getServings() != null ? recipeRequest.getServings() : 0);
        recipe.setIsVegetarian(recipeRequest.getIsVegetarian() != null ? recipeRequest.getIsVegetarian() : false);

        recipe.getIngredients().clear();
        recipe.getInstructions().clear();

        recipe.getIngredients().addAll(mapIngredients(recipeRequest.getIngredients(), recipe));
        recipe.getInstructions().addAll(mapInstructions(recipeRequest.getInstructions(), recipe));
    }

    public RecipeResponse toResponse(Recipe recipe) {

        Set<IngredientResponse> ingredientsResponse = recipe.getIngredients() == null ? Collections.emptySet() :
                recipe.getIngredients().stream().map(ingredients -> IngredientResponse.builder()
                            .name(ingredients.getName())
                            .quantity(ingredients.getQuantity())
                            .unit(ingredients.getUnit())
                            .build())
                .collect(Collectors.toSet());

        Set<InstructionResponse> instructionsResponse = recipe.getInstructions() == null ? Collections.emptySet() :
                recipe.getInstructions().stream().sorted(Comparator.comparingInt(Instruction::getStepOrder))
                        .map(instruction -> InstructionResponse.builder()
                            .stepOrder(instruction.getStepOrder())
                            .description(instruction.getDescription())
                            .build())
                        .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        return RecipeResponse.builder()
                .id(recipe.getId())
                .userId(recipe.getUserId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .servings(recipe.getServings())
                .isVegetarian(recipe.getIsVegetarian())
                .ingredients(ingredientsResponse)
                .instructions(instructionsResponse)
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .build();
    }

    private Set<Ingredient> mapIngredients(Set<IngredientRequest> ingredientsRequest, Recipe recipe) {
        if (ingredientsRequest == null || ingredientsRequest.isEmpty()) {
            return new HashSet<>();
        }
        return ingredientsRequest.stream()
                .map(req -> Ingredient.builder()
                        .name(req.getName())
                        .quantity(req.getQuantity())
                        .unit(req.getUnit())
                        .recipe(recipe)
                        .build())
                .collect(Collectors.toSet());
    }

    private Set<Instruction> mapInstructions(Set<InstructionRequest> instructionsRequest, Recipe recipe) {
        if (instructionsRequest == null || instructionsRequest.isEmpty()) {
            return new HashSet<>();
        }

        return instructionsRequest.stream()
                .map(req -> Instruction.builder()
                        .stepOrder(req.getStepOrder())
                        .description(req.getDescription())
                        .recipe(recipe)
                        .build())
                .collect(Collectors.toSet());
    }
}
