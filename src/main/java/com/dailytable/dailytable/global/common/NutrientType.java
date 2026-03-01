package com.dailytable.dailytable.global.common;

import lombok.Getter;

@Getter
public enum NutrientType {
    CALORIES("calories", "kcal"),
    PROTEIN("protein", "g"),
    FAT("fat", "g"),
    CARBS("carbs", "g"),
    NOTE("nutrient","");

    private final String name;
    private final String unit;

    NutrientType(String name, String unit) {
        this.name = name;
        this.unit = unit;
    }

}