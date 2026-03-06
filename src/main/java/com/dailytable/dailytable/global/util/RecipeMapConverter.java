package com.dailytable.dailytable.global.util;

import java.util.Map;

public class RecipeMapConverter {
    public static final Map<String, Integer> PURPOSE_MAP = Map.of(
            "ガッツリ系", 1, "ダイエット", 2, "ヘルシー", 3, "おつまみ", 4
    );
    public  static final Map<String, Integer> CUISINE_MAP = Map.of(
            "なんでも ", 1, "日本料理", 2, "韓国料理", 3, "中華料理", 4, "洋食", 5, "東南アジア料理", 6
    );
//    public  static final Map<String, Integer> DIFFICULTY_MAP = Map.of(
//            "なんでも", 0, "低", 1, "中", 2, "高", 3
//    );
    public  static final Map<String, String> DIFFICULTY_AI_MAP = Map.of(
            "低", "LOW", "中", "MEDIUM", "高", "HIGH", "なんでも", "ANY"
    );
    public  static final Map<String, Integer> DIFFICULTY_RESULT_MAP = Map.of(
            "LOW", 1, "MEDIUM", 2, "HIGH", 3
    );
    public  static final Map<String, String> DIFFICULTY_LABEL_MAP = Map.of(
            "LOW", "低", "MEDIUM", "中", "HIGH", "高"
    );
    public  static final Map<Integer, String> CUISINE_STYLE_MAP = Map.of(
            1, "Any", 2, "韓国料理", 3, "日本料理", 4, "中華料理", 5, "洋食", 6, "東南アジア料理"
    );
}
