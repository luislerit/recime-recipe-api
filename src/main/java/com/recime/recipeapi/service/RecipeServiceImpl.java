package com.recime.recipeapi.service;

import com.recime.recipeapi.dto.request.RecipeRequest;
import com.recime.recipeapi.dto.response.RecipeResponse;
import com.recime.recipeapi.entity.Recipe;
import com.recime.recipeapi.exception.RecipeNotFoundException;
import com.recime.recipeapi.mapper.RecipeMapper;
import com.recime.recipeapi.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    @Override
    @Transactional
    public RecipeResponse create(RecipeRequest recipeRequest, UUID userId) {
        Recipe recipe = recipeMapper.toEntity(recipeRequest, userId);
        Recipe savedRecipe = recipeRepository.save(recipe);
        return recipeMapper.toResponse(savedRecipe);
    }

    @Override
    @Transactional
    public RecipeResponse update(RecipeRequest recipeRequest, Long recipeId, UUID userId) {
        Recipe recipe = recipeRepository.findByIdAndUserId(recipeId, userId).orElseThrow(() ->
                new RecipeNotFoundException("Recipe not found"));
        recipeMapper.updateEntity(recipe, recipeRequest);
        Recipe savedRecipe = recipeRepository.save(recipe);
        return recipeMapper.toResponse(savedRecipe);
    }

    @Override
    @Transactional
    public void delete(Long recipeId, UUID userId) {
        Recipe recipe = recipeRepository.findByIdAndUserId(recipeId, userId).orElseThrow(() ->
                new RecipeNotFoundException("Recipe not found"));
        recipeRepository.delete(recipe);
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeResponse findById(Long recipeId, UUID userId) {
        Recipe recipe = recipeRepository.findByIdAndUserId(recipeId, userId).orElseThrow(() ->
                new RecipeNotFoundException("Recipe not found"));
        return recipeMapper.toResponse(recipe);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecipeResponse> search(UUID userId, Boolean isVegetarian, Integer servings, Set<String> include,
                                       Set<String> exclude, String instructions, Pageable pageable) {
        return null;
    }
}
