package com.dailytable.dailytable.domain.ingredient;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IngredientRepository {

    String findNormalizedByAlias(@Param("aliasName") String aliasName);

    void insertAlias(IngredientEntity.Alias alias);

    void insertIngredient(@Param("normalizedName") String normalizedName);

    boolean existsByNormalizedName(@Param("normalizedName") String normalizedName);
}
