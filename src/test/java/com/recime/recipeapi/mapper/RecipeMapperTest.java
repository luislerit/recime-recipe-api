package com.recime.recipeapi.mapper;

import com.recime.recipeapi.dto.request.IngredientRequest;
import com.recime.recipeapi.dto.request.InstructionRequest;
import com.recime.recipeapi.dto.request.RecipeRequest;
import com.recime.recipeapi.dto.response.InstructionResponse;
import com.recime.recipeapi.dto.response.RecipeResponse;
import com.recime.recipeapi.entity.Instruction;
import com.recime.recipeapi.entity.Recipe;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeMapperTest {

    private final RecipeMapper mapper = new RecipeMapper();
    private final UUID userId = UUID.randomUUID();

    @Test
    void toEntity_mapsAllFieldsAndOwnerUserId() {
        RecipeRequest request = RecipeRequest.builder()
                .title("Pasta")
                .description("Quick dinner")
                .servings(4)
                .isVegetarian(true)
                .ingredients(Set.of(IngredientRequest.builder().name("tomato").quantity("2").unit("pcs").build()))
                .instructions(Set.of(InstructionRequest.builder().stepOrder(1).description("Boil water").build()))
                .build();

        Recipe entity = mapper.toEntity(request, userId);

        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getTitle()).isEqualTo("Pasta");
        assertThat(entity.getDescription()).isEqualTo("Quick dinner");
        assertThat(entity.getServings()).isEqualTo(4);
        assertThat(entity.getIsVegetarian()).isTrue();
        assertThat(entity.getIngredients()).hasSize(1);
        assertThat(entity.getInstructions()).hasSize(1);
        assertThat(entity.getIngredients().iterator().next().getRecipe()).isSameAs(entity);
        assertThat(entity.getInstructions().iterator().next().getRecipe()).isSameAs(entity);
    }

    @Test
    void toEntity_servingsNullWithEmptyIngredients_defaultsToZero() {
        RecipeRequest request = RecipeRequest.builder().title("Empty").build();

        Recipe entity = mapper.toEntity(request, userId);

        assertThat(entity.getServings()).isZero();
    }

    @Test
    void toEntity_servingsNullWithIngredients_defaultsToOne() {
        RecipeRequest request = RecipeRequest.builder()
                .title("Salt")
                .ingredients(Set.of(IngredientRequest.builder().name("salt").build()))
                .build();

        Recipe entity = mapper.toEntity(request, userId);

        assertThat(entity.getServings()).isEqualTo(1);
    }

    @Test
    void toEntity_explicitServings_wins() {
        RecipeRequest request = RecipeRequest.builder()
                .title("Big batch")
                .servings(12)
                .ingredients(Set.of(IngredientRequest.builder().name("flour").build()))
                .build();

        Recipe entity = mapper.toEntity(request, userId);

        assertThat(entity.getServings()).isEqualTo(12);
    }

    @Test
    void toEntity_nullIsVegetarian_defaultsToFalse() {
        RecipeRequest request = RecipeRequest.builder().title("Mystery").build();

        Recipe entity = mapper.toEntity(request, userId);

        assertThat(entity.getIsVegetarian()).isFalse();
    }

    @Test
    void updateEntity_replacesChildrenCompletely() {
        Recipe recipe = Recipe.builder()
                .userId(userId)
                .title("Old")
                .description("Old desc")
                .servings(1)
                .isVegetarian(false)
                .build();
        recipe.setIngredients(new LinkedHashSet<>());
        recipe.setInstructions(new LinkedHashSet<>());

        RecipeRequest request = RecipeRequest.builder()
                .title("New")
                .description("New desc")
                .servings(3)
                .isVegetarian(true)
                .ingredients(Set.of(IngredientRequest.builder().name("rice").build()))
                .instructions(Set.of(InstructionRequest.builder().stepOrder(1).description("Rinse").build()))
                .build();

        mapper.updateEntity(recipe, request);

        assertThat(recipe.getTitle()).isEqualTo("New");
        assertThat(recipe.getDescription()).isEqualTo("New desc");
        assertThat(recipe.getServings()).isEqualTo(3);
        assertThat(recipe.getIsVegetarian()).isTrue();
        assertThat(recipe.getIngredients()).hasSize(1);
        assertThat(recipe.getInstructions()).hasSize(1);
    }

    @Test
    void toResponse_sortsInstructionsByStepOrder() {
        Recipe recipe = Recipe.builder()
                .id(1L)
                .userId(userId)
                .title("Steps")
                .servings(1)
                .isVegetarian(false)
                .build();

        Instruction step3 = Instruction.builder().stepOrder(3).description("Third").recipe(recipe).build();
        Instruction step1 = Instruction.builder().stepOrder(1).description("First").recipe(recipe).build();
        Instruction step2 = Instruction.builder().stepOrder(2).description("Second").recipe(recipe).build();
        recipe.setInstructions(new LinkedHashSet<>(List.of(step3, step1, step2)));

        RecipeResponse response = mapper.toResponse(recipe);

        List<Integer> ordered = response.getInstructions().stream()
                .map(InstructionResponse::getStepOrder)
                .collect(Collectors.toList());
        assertThat(ordered).containsExactly(1, 2, 3);
    }

    @Test
    void toResponse_nullCollections_returnsEmptySets() {
        Recipe recipe = Recipe.builder()
                .id(1L)
                .userId(userId)
                .title("Bare")
                .servings(0)
                .isVegetarian(false)
                .build();
        recipe.setIngredients(null);
        recipe.setInstructions(null);

        RecipeResponse response = mapper.toResponse(recipe);

        assertThat(response.getIngredients()).isEmpty();
        assertThat(response.getInstructions()).isEmpty();
    }
}
