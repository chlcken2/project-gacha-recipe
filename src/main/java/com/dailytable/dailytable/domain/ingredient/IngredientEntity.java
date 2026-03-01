package com.dailytable.dailytable.domain.ingredient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientEntity {
    private Long id;
    private String normalizedName;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alias {
        private Long id;
        private String aliasName;
        private String normalizedName;
        private LocalDateTime createdAt;
    }
}
