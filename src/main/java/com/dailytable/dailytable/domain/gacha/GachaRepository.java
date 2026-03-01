package com.dailytable.dailytable.domain.gacha;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GachaRepository {

    int countTodayGenerations(@Param("userId") Long userId);
}
