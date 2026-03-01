package com.dailytable.dailytable.global.common;

import lombok.Getter;

@Getter
public enum IngredientType {
    INGREDIENT_TYPE("ingredients"),
    NAME("name"),
    AMOUNT("amount"),
    UNIT("unit");

    private final String name;

    IngredientType(String name) {
        this.name = name;
    }
}
