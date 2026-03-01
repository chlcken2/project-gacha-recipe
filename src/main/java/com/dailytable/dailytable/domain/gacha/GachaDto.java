package com.dailytable.dailytable.domain.gacha;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class GachaDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateRequest {
        private List<IngredientInput> ingredients;
        private List<IngredientInput> sauces;
        private String purpose;
        private String cuisine;
        private String difficulty;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientInput {
        private String name;
        private String amount;
        private String unit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateResponse {
        private boolean success;
        private String message;
        private RecipeResult recipe;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeResult {
        private Long id;
        private String title;
        private String titleImage;
        private String summary;
        private String difficulty;
        private String difficultyLabel;
        private Integer cookingTime;
        private String purpose;
        private String cuisine;
        private List<IngredientResult> ingredients;
        private List<String> steps;
        private NutrientResult nutrients;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientResult {
        private String name;
        private BigDecimal amount;
        private String unit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutrientResult {
        private Integer calories;
        private BigDecimal protein;
        private BigDecimal fat;
        private BigDecimal carbs;
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCountResponse {
        private int count;
        private int max;
        private boolean canGenerate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationResult {
        private boolean valid;
        private String name;
        private String reason;
    }
}
