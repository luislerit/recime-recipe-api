package com.recime.recipeapi.service;

import com.recime.recipeapi.dto.request.RecipeRequest;
import com.recime.recipeapi.dto.response.RecipeResponse;
import com.recime.recipeapi.entity.Recipe;
import com.recime.recipeapi.exception.RecipeNotFoundException;
import com.recime.recipeapi.mapper.RecipeMapper;
import com.recime.recipeapi.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeMapper recipeMapper;

    @InjectMocks
    private RecipeServiceImpl service;

    private UUID userId;
    private Recipe recipe;
    private RecipeResponse response;
    private RecipeRequest request;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        recipe = Recipe.builder().id(1L).userId(userId).title("Pasta").build();
        response = RecipeResponse.builder().id(1L).userId(userId).title("Pasta").build();
        request = RecipeRequest.builder().title("Pasta").build();
    }

    @Test
    void create_savesAndReturnsMappedResponse() {
        when(recipeMapper.toEntity(request, userId)).thenReturn(recipe);
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toResponse(recipe)).thenReturn(response);

        RecipeResponse result = service.create(request, userId);

        assertThat(result).isSameAs(response);
        verify(recipeRepository).save(recipe);
    }

    @Test
    void findById_returnsResponseWhenFound() {
        when(recipeRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(recipe));
        when(recipeMapper.toResponse(recipe)).thenReturn(response);

        RecipeResponse result = service.findById(1L, userId);

        assertThat(result).isSameAs(response);
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(recipeRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(1L, userId))
                .isInstanceOf(RecipeNotFoundException.class)
                .hasMessage("Recipe not found");
    }

    @Test
    void search_composesSpecificationAndMapsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Recipe> page = new PageImpl<>(List.of(recipe));
        when(recipeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(recipeMapper.toResponse(recipe)).thenReturn(response);

        Page<RecipeResponse> result = service.search(userId, true, 4,
                Set.of("tomato"), Set.of("peanut"), "boil", pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void update_appliesMapperAndSaves() {
        when(recipeRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toResponse(recipe)).thenReturn(response);

        RecipeResponse result = service.update(request, 1L, userId);

        verify(recipeMapper).updateEntity(recipe, request);
        verify(recipeRepository).save(recipe);
        assertThat(result).isSameAs(response);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(recipeRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(request, 1L, userId))
                .isInstanceOf(RecipeNotFoundException.class);
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void delete_deletesWhenFound() {
        when(recipeRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(recipe));

        service.delete(1L, userId);

        verify(recipeRepository).delete(recipe);
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(recipeRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L, userId))
                .isInstanceOf(RecipeNotFoundException.class);
        verify(recipeRepository, never()).delete(any(Recipe.class));
    }
}
