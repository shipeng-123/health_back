package org.example.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.health.entity.SportRecord;

import java.math.BigDecimal;
import java.time.LocalDate;

@Mapper
public interface SportRecordMapper extends BaseMapper<SportRecord> {

    @Select("""
        SELECT COALESCE(SUM(calories), 0)
        FROM sport_record
        WHERE user_id = #{userId}
          AND record_date = #{date}
    """)
    BigDecimal sumCaloriesByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}