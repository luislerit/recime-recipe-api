package com.recime.recipeapi.service;

import com.recime.recipeapi.dto.request.RecipeRequest;
import com.recime.recipeapi.dto.response.RecipeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

public interface RecipeService {

    RecipeResponse create(RecipeRequest recipeRequest, UUID userId);
    RecipeResponse update(RecipeRequest recipeRequest, Long recipeId, UUID usedId);
    void delete(Long recipeId, UUID usedId);
    RecipeResponse findById(Long recipeId, UUID userId);

    Page<RecipeResponse> search(
            UUID userId,
            Boolean isVegetarian,
            Integer servings,
            Set<String> include,
            Set<String> exclude,
            String instructions,
            Pageable pageable
    );

}
