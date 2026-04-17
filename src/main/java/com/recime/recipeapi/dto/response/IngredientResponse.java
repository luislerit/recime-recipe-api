package com.recime.recipeapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientResponse {

    private String name;
    private String quantity;
    private String unit;
}
