package com.recime.recipeapi.exception;

public class RecipeNotFoundException extends RuntimeException{
    public RecipeNotFoundException(){}
    public RecipeNotFoundException(String message){
        super(message);
    }
}
