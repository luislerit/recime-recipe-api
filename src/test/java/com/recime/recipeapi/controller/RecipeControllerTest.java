package com.recime.recipeapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recime.recipeapi.dto.request.IngredientRequest;
import com.recime.recipeapi.dto.request.InstructionRequest;
import com.recime.recipeapi.dto.request.RecipeRequest;
import com.recime.recipeapi.dto.response.RecipeResponse;
import com.recime.recipeapi.exception.RecipeNotFoundException;
import com.recime.recipeapi.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecipeService recipeService;

    private UUID userId;
    private RecipeRequest validRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        validRequest = RecipeRequest.builder()
                .title("Pasta")
                .description("Quick dinner")
                .servings(4)
                .isVegetarian(true)
                .ingredients(Set.of(IngredientRequest.builder().name("tomato").quantity("2").unit("pcs").build()))
                .instructions(Set.of(InstructionRequest.builder().stepOrder(1).description("Boil water").build()))
                .build();
    }

    @Test
    void create_returns201WithLocationAndBody() throws Exception {
        RecipeResponse response = RecipeResponse.builder().id(42L).userId(userId).title("Pasta").build();
        when(recipeService.create(any(), eq(userId))).thenReturn(response);

        mockMvc.perform(post("/api/v1/recipes")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/recipes/42")))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.title").value("Pasta"));
    }

    @Test
    void create_returns400WhenTitleBlank() throws Exception {
        RecipeRequest bad = RecipeRequest.builder().title("").build();

        mockMvc.perform(post("/api/v1/recipes")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("title")));
    }

    @Test
    void create_returns400WhenDuplicateStepOrders() throws Exception {
        RecipeRequest bad = RecipeRequest.builder()
                .title("Pasta")
                .instructions(Set.of(
                        InstructionRequest.builder().stepOrder(1).description("Boil water").build(),
                        InstructionRequest.builder().stepOrder(1).description("Add salt").build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/recipes")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void create_returns400WhenNestedIngredientNameBlank() throws Exception {
        RecipeRequest bad = RecipeRequest.builder()
                .title("Pasta")
                .ingredients(Set.of(IngredientRequest.builder().name("").build()))
                .build();

        mockMvc.perform(post("/api/v1/recipes")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenUserIdHeaderMissing() throws Exception {
        mockMvc.perform(post("/api/v1/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("X-User-Id")));
    }

    @Test
    void findById_returns200() throws Exception {
        RecipeResponse response = RecipeResponse.builder().id(1L).userId(userId).title("Pasta").build();
        when(recipeService.findById(1L, userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/recipes/1").header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Pasta"));
    }

    @Test
    void findById_returns404WhenServiceThrows() throws Exception {
        when(recipeService.findById(1L, userId)).thenThrow(new RecipeNotFoundException("Recipe not found"));

        mockMvc.perform(get("/api/v1/recipes/1").header("X-User-Id", userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Recipe not found"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void search_passesAllFiltersToService() throws Exception {
        Page<RecipeResponse> page = new PageImpl<>(List.of());
        when(recipeService.search(any(), any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/recipes")
                        .header("X-User-Id", userId.toString())
                        .param("isVegetarian", "true")
                        .param("servings", "4")
                        .param("include", "tomato", "basil")
                        .param("exclude", "peanut")
                        .param("instruction", "boil"))
                .andExpect(status().isOk());

        ArgumentCaptor<Set<String>> includeCaptor = ArgumentCaptor.forClass(Set.class);
        ArgumentCaptor<Set<String>> excludeCaptor = ArgumentCaptor.forClass(Set.class);
        org.mockito.Mockito.verify(recipeService).search(
                eq(userId), eq(true), eq(4),
                includeCaptor.capture(), excludeCaptor.capture(),
                eq("boil"), any(Pageable.class));
        assertThat(includeCaptor.getValue()).containsExactlyInAnyOrder("tomato", "basil");
        assertThat(excludeCaptor.getValue()).containsExactly("peanut");
    }

    @Test
    void search_returns200WithEmptyPageWhenNoFilters() throws Exception {
        Page<RecipeResponse> page = new PageImpl<>(List.of());
        when(recipeService.search(any(), any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/recipes").header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void update_returns200() throws Exception {
        RecipeResponse response = RecipeResponse.builder().id(1L).userId(userId).title("Pasta").build();
        when(recipeService.update(any(), eq(1L), eq(userId))).thenReturn(response);

        mockMvc.perform(put("/api/v1/recipes/1")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_returns404WhenNotFound() throws Exception {
        when(recipeService.update(any(), eq(1L), eq(userId)))
                .thenThrow(new RecipeNotFoundException("Recipe not found"));

        mockMvc.perform(put("/api/v1/recipes/1")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/recipes/1").header("X-User-Id", userId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_returns404WhenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new RecipeNotFoundException("Recipe not found"))
                .when(recipeService).delete(1L, userId);

        mockMvc.perform(delete("/api/v1/recipes/1").header("X-User-Id", userId.toString()))
                .andExpect(status().isNotFound());
    }
}
