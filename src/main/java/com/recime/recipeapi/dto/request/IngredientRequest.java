package com.recime.recipeapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IngredientRequest {

    @NotBlank
    @Size(max = 50)
    private String name;
    @Size(max = 50)
    private String quantity;
    @Size(max = 50)
    private String unit;

}
