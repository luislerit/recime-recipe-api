package com.recime.recipeapi.controller;

import com.recime.recipeapi.dto.request.RecipeRequest;
import com.recime.recipeapi.dto.response.RecipeResponse;
import com.recime.recipeapi.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/recipes")
@Tag(name = "Recipes", description = "CRUD and search for cooking recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping
    @Operation(summary = "Create a recipe")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Recipe created"),
            @ApiResponse(responseCode = "400", description = "Validation failed or missing header")
    })
    public ResponseEntity<RecipeResponse> create(
            @Parameter(description = "Owner user id", required = true) @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid RecipeRequest request) {
        RecipeResponse recipeResponse = recipeService.create(request, userId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(recipeResponse.getId())
                .toUri();

        return ResponseEntity.created(location).body(recipeResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch one recipe by id (scoped to owner)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipe found"),
            @ApiResponse(responseCode = "404", description = "Recipe not found for this user")
    })
    public ResponseEntity<RecipeResponse> findById(@PathVariable("id") Long recipeId,
                                                   @Parameter(description = "Owner user id", required = true) @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(recipeService.findById(recipeId, userId));
    }

    @GetMapping
    @Operation(summary = "List + search recipes",
            description = "All filters are optional and AND-combined. Pagination via standard Spring params.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of recipes")
    })
    public ResponseEntity<Page<RecipeResponse>> search(
            @Parameter(description = "Owner user id", required = true) @RequestHeader("X-User-Id") UUID userId,
            @Parameter(description = "Filter by vegetarian flag") @RequestParam(required = false) Boolean isVegetarian,
            @Parameter(description = "Exact match on servings") @RequestParam(required = false) Integer servings,
            @Parameter(description = "Recipes must contain ALL of these ingredient names (case-insensitive)") @RequestParam(required = false) Set<String> include,
            @Parameter(description = "Recipes must contain NONE of these ingredient names (case-insensitive)") @RequestParam(required = false) Set<String> exclude,
            @Parameter(description = "Keyword search over instruction descriptions (case-insensitive)") @RequestParam(required = false) String instruction,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(
                recipeService.search(userId, isVegetarian, servings, include, exclude, instruction, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a recipe (full update)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipe updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Recipe not found for this user")
    })
    public ResponseEntity<RecipeResponse> update(@PathVariable("id") Long recipeId,
                                                 @Parameter(description = "Owner user id", required = true) @RequestHeader("X-User-Id") UUID userId,
                                                 @RequestBody @Valid RecipeRequest request) {
        return ResponseEntity.ok(recipeService.update(request, recipeId, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a recipe")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Recipe deleted"),
            @ApiResponse(responseCode = "404", description = "Recipe not found for this user")
    })
    public ResponseEntity<Void> delete(@PathVariable("id") Long recipeId,
                                       @Parameter(description = "Owner user id", required = true) @RequestHeader("X-User-Id") UUID userId) {
        recipeService.delete(recipeId, userId);
        return ResponseEntity.noContent().build();
    }

}
