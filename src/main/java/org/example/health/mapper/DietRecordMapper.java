package org.example.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.health.dto.DietRecordItemResp;
import org.example.health.entity.DietRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DietRecordMapper extends BaseMapper<DietRecord> {

    @Select("""
        SELECT COUNT(1)
        FROM diet_record
        WHERE user_id = #{userId}
          AND record_date = #{date}
    """)
    int countByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("""
        SELECT COALESCE(SUM(total_calories), 0)
        FROM diet_record
        WHERE user_id = #{userId}
          AND record_date = #{date}
    """)
    BigDecimal sumCaloriesByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("""
        SELECT
            dr.id AS id,
            dr.food_item_id AS foodItemId,
            fi.food_name AS foodName,
            dr.meal_type AS mealType,
            dr.intake_gram AS intakeGram,
            dr.calorie_per_100g AS caloriePer100g,
            dr.total_calories AS totalCalories,
            dr.remark AS remark,
            dr.create_time AS createTime
        FROM diet_record dr
        LEFT JOIN food_item fi ON fi.id = dr.food_item_id
        WHERE dr.user_id = #{userId}
          AND dr.record_date = #{date}
        ORDER BY dr.create_time DESC, dr.id DESC
    """)
    List<DietRecordItemResp> findItemsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}