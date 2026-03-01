package com.dailytable.dailytable.global.util;

import java.util.Map;

public class RecipeMapConverter {
    public static final Map<String, Integer> PURPOSE_MAP = Map.of(
            "가정식", 1,"속세의맛", 2, "다이어트", 3, "건강식", 4, "술안주", 5
    );
    public static final Map<String, Integer> CUISINE_MAP = Map.of(
            "상관없음", 1, "한식", 2, "일식", 3, "중식", 4, "양식", 5, "동남아", 6
    );
    public static final Map<String, Integer> DIFFICULTY_MAP = Map.of(
            "상관없음", 1, "하", 2, "중간", 3, "상", 4
    );
}
