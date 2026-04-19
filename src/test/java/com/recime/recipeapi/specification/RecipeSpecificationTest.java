package com.recime.recipeapi.specification;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeSpecificationTest {

    @Test
    void byUserId_returnsNonNullSpec() {
        assertThat(RecipeSpecification.byUserId(UUID.randomUUID())).isNotNull();
    }

    @Test
    void byVegetarian_nullReturnsNoOpSpec() {
        assertThat(RecipeSpecification.byVegetarian(null)).isNull();
        assertThat(RecipeSpecification.byVegetarian(true)).isNotNull();
    }

    @Test
    void byServings_nullReturnsNoOpSpec() {
        assertThat(RecipeSpecification.byServings(null)).isNull();
        assertThat(RecipeSpecification.byServings(4)).isNotNull();
    }

    @Test
    void byIncludeIngredients_nullOrEmptyReturnsNoOpSpec() {
        assertThat(RecipeSpecification.byIncludeIngredients(null)).isNull();
        assertThat(RecipeSpecification.byIncludeIngredients(Collections.emptySet())).isNull();
        assertThat(RecipeSpecification.byIncludeIngredients(Set.of("tomato"))).isNotNull();
    }

    @Test
    void byExcludeIngredients_nullOrEmptyReturnsNoOpSpec() {
        assertThat(RecipeSpecification.byExcludeIngredients(null)).isNull();
        assertThat(RecipeSpecification.byExcludeIngredients(Collections.emptySet())).isNull();
        assertThat(RecipeSpecification.byExcludeIngredients(Set.of("peanut"))).isNotNull();
    }

    @Test
    void byInstructionKeyword_nullOrBlankReturnsNoOpSpec() {
        assertThat(RecipeSpecification.byInstructionKeyword(null)).isNull();
        assertThat(RecipeSpecification.byInstructionKeyword("")).isNull();
        assertThat(RecipeSpecification.byInstructionKeyword("   ")).isNull();
        assertThat(RecipeSpecification.byInstructionKeyword("boil")).isNotNull();
    }
}
