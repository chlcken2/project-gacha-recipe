package com.dailytable.dailytable.domain.recipe;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecipeRepository {

    void insertRecipe(RecipeEntity recipe);

    void insertRecipeIngredient(RecipeEntity.RecipeIngredient ingredient);

    void insertRecipeStep(RecipeEntity.RecipeStep step);

    void insertRecipeNutrient(RecipeEntity.RecipeNutrient nutrient);

    RecipeEntity findById(@Param("id") Long id);

    List<RecipeEntity.RecipeIngredient> findIngredientsByRecipeId(@Param("recipeId") Long recipeId);

    List<RecipeEntity.RecipeStep> findStepsByRecipeId(@Param("recipeId") Long recipeId);

    List<RecipeEntity.RecipeNutrient> findNutrientsByRecipeId(@Param("recipeId") Long recipeId);

    void updatePublicStatus(@Param("id") Long id, @Param("isPublic") boolean isPublic);
}
