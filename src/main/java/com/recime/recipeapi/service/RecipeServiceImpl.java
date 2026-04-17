package com.recime.recipeapi.service;

import com.recime.recipeapi.dto.request.RecipeRequest;
import com.recime.recipeapi.dto.response.RecipeResponse;
import com.recime.recipeapi.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RecipeServiceImpl implements RecipeService{

    private final RecipeRepository recipeRepository;

    @Override
    public RecipeResponse create(RecipeRequest recipeRequest, UUID userId) {
        return null;
    }

    @Override
    public RecipeResponse update(RecipeRequest recipeRequest, Long recipeId, UUID usedId) {
        return null;
    }

    @Override
    public void delete(Long recipeId, UUID usedId) {

    }

    @Override
    public RecipeResponse findById(Long recipeId, UUID userId) {
        return null;
    }

    @Override
    public Page<RecipeResponse> search(UUID userId, Boolean isVegetarian, Integer servings, Set<String> include, Set<String> exclude, String instructions, Pageable pageable) {
        return null;
    }
}
