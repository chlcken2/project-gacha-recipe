package com.dailytable.dailytable.domain.recipe;

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
public class RecipeEntity {
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
    private int viewCount;
    private int commentCount;
    private int likeCount;
    private int popularityScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // Join fields for display
    private String difficultyName;
    private String purposeName;
    private String cuisineName;
    private String userNickname;

    // Related data (populated by service)
    private List<RecipeIngredient> ingredients;
    private List<RecipeStep> steps;
    private List<RecipeNutrient> nutrients;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeIngredient {
        private Long id;
        private Long recipeId;
        private String name;
        private String normalizedName;
        private BigDecimal quantity;
        private String unit;
        private boolean aiGenerated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeStep {
        private Long id;
        private Long recipeId;
        private Integer stepOrder;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeNutrient {
        private Long id;
        private Long recipeId;
        private String nutrientName;
        private BigDecimal amount;
        private String unit;
    }
}
