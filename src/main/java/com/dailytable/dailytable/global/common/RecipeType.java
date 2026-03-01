package com.dailytable.dailytable.global.common;

import lombok.Getter;

@Getter
public enum RecipeType {
    RECIPE_TYPE("recipe"),
    TITLE("title"),
    SUMMARY("summary"),
    DIFFICULTY("difficulty"),
    ESTIMATED_TIME_MINUTES("estimatedTimeMinutes");

    private final String name;

    RecipeType(String name){
        this.name = name;
    }



}
