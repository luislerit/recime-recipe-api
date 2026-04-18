package com.recime.recipeapi.controller;

import com.recime.recipeapi.dto.request.RecipeRequest;
import com.recime.recipeapi.dto.response.RecipeResponse;
import com.recime.recipeapi.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping
    public ResponseEntity<RecipeResponse> create(@RequestHeader("X-User-Id") UUID userId,
                                                 @RequestBody RecipeRequest request) {
        return null;
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> update(@PathVariable("id") Long recipeId,
                                                 @RequestHeader("X-User-Id") UUID userId,
                                                 @RequestBody RecipeRequest request) {
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long recipeId,
                                       @RequestHeader("X-User-Id") UUID userId) {
        return null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> findById(@PathVariable("id") Long recipeId,
                                                   @RequestHeader("X-User-Id") UUID userId) {
        return null;
    }

    @GetMapping
    public ResponseEntity<Page<RecipeResponse>> search(@RequestHeader("X-User-Id") UUID userId,
                                                       @RequestParam(required = false) Boolean isVegetarian,
                                                       @RequestParam(required = false) Integer servings,
                                                       @RequestParam(required = false) Set<String> include,
                                                       @RequestParam(required = false) Set<String> exclude,
                                                       @RequestParam(required = false) String instruction,
                                                       @ParameterObject @PageableDefault(
                                                               size = 10,
                                                               sort = "createdAt",
                                                               direction = Sort.Direction.DESC)
                                                           Pageable pageable) {
        return null;
    }
}
