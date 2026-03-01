package com.dailytable.dailytable.domain.gacha;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GachaEntity {

    private Long id;
    private Long userId;
    private String title;
    private String titleImage;
    private String description;
    private Integer cookingTime;
    private Integer difficultyId;
    private Integer purposeId;
    private Integer cuisineId;
    private boolean aiGenerated;
    private boolean isPublic;
    private LocalDateTime createdAt;

    private List<Ingredient> ingredients;
    private List<String> steps;
    private Nutrient nutrient;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ingredient {
        private Long id;
        private Long recipeId;
        private String name;
        private String normalizedName;
        private BigDecimal quantity;
        private String unit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Step {
        private Long id;
        private Long recipeId;
        private Integer stepOrder;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Nutrient {
        private Integer calories;
        private BigDecimal protein;
        private BigDecimal fat;
        private BigDecimal carbs;
        private String note;
    }
}
