package com.recime.recipeapi.service;

import com.recime.recipeapi.dto.request.RecipeRequest;
import com.recime.recipeapi.dto.response.RecipeResponse;
import com.recime.recipeapi.entity.Recipe;
import com.recime.recipeapi.exception.RecipeNotFoundException;
import com.recime.recipeapi.mapper.RecipeMapper;
import com.recime.recipeapi.repository.RecipeRepository;
import com.recime.recipeapi.specification.RecipeSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
@Slf4j
@RequiredArgsConstructor
@Service
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    @Override
    @Transactional
    public RecipeResponse create(RecipeRequest recipeRequest, UUID userId) {
        log.info("Creating recipe for userId={}", userId);
        Recipe recipe = recipeMapper.toEntity(recipeRequest, userId);
        Recipe savedRecipe = recipeRepository.save(recipe);
        log.info("Created recipeId={} for userId={}", savedRecipe.getId(), userId);
        return recipeMapper.toResponse(savedRecipe);
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeResponse findById(Long recipeId, UUID userId) {
        Recipe recipe = recipeRepository.findByIdAndUserId(recipeId, userId)
                .orElseThrow(() -> {
                    log.warn("Recipe not found: recipeId={} userId={}", recipeId, userId);
                    return new RecipeNotFoundException("Recipe not found");
                });
        return recipeMapper.toResponse(recipe);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecipeResponse> search(UUID userId, Boolean isVegetarian, Integer servings, Set<String> include,
                                       Set<String> exclude, String instructions, Pageable pageable) {

        Specification<Recipe> spec = RecipeSpecification.byUserId(userId)
                .and(RecipeSpecification.byVegetarian(isVegetarian))
                .and(RecipeSpecification.byServings(servings))
                .and(RecipeSpecification.byIncludeIngredients(include))
                .and(RecipeSpecification.byExcludeIngredients(exclude))
                .and(RecipeSpecification.byInstructionKeyword(instructions));

        return recipeRepository.findAll(spec, pageable).map(recipeMapper::toResponse);
    }

    @Override
    @Transactional
    public RecipeResponse update(RecipeRequest recipeRequest, Long recipeId, UUID userId) {
        Recipe recipe = recipeRepository.findByIdAndUserId(recipeId, userId)
                .orElseThrow(() -> {
                    log.warn("Recipe not found for update: recipeId={} userId={}", recipeId, userId);
                    return new RecipeNotFoundException("Recipe not found");
                });
        recipeMapper.updateEntity(recipe, recipeRequest);
        Recipe savedRecipe = recipeRepository.save(recipe);
        log.info("Updated recipeId={} for userId={}", recipeId, userId);
        return recipeMapper.toResponse(savedRecipe);
    }

    @Override
    @Transactional
    public void delete(Long recipeId, UUID userId) {
        Recipe recipe = recipeRepository.findByIdAndUserId(recipeId, userId)
                .orElseThrow(() -> {
                    log.warn("Recipe not found for update: recipeId={} userId={}", recipeId, userId);
                    return new RecipeNotFoundException("Recipe not found");
                });
        recipeRepository.delete(recipe);
        log.info("Deleted recipeId={} for userId={}", recipeId, userId);
    }
}
