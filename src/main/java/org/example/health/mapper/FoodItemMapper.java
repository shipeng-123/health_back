package org.example.health.mapper;

import org.apache.ibatis.annotations.*;
import org.example.health.dto.FoodItemResp;
import org.example.health.entity.FoodItem;

import java.util.List;

@Mapper
public interface FoodItemMapper {

    @Select({
            "<script>",
            "SELECT",
            " id,",
            " user_id AS userId,",
            " is_builtin AS isBuiltin,",
            " food_name AS foodName,",
            " category,",
            " calorie_per_100g AS caloriePer100g,",
            " protein_per_100g AS proteinPer100g,",
            " fat_per_100g AS fatPer100g,",
            " carb_per_100g AS carbPer100g",
            "FROM food_item",
            "WHERE status = 1",
            "  AND deleted = 0",
            "  AND (is_builtin = 1 OR user_id = #{userId})",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND food_name LIKE CONCAT('%', #{keyword}, '%')",
            "  </if>",
            "ORDER BY is_builtin DESC, id DESC",
            "LIMIT #{limit}",
            "</script>"
    })
    List<FoodItemResp> search(@Param("userId") Long userId,
                              @Param("keyword") String keyword,
                              @Param("limit") Integer limit);

    @Select({
            "SELECT",
            " id,",
            " user_id AS userId,",
            " food_name AS foodName,",
            " category,",
            " calorie_per_100g AS caloriePer100g,",
            " protein_per_100g AS proteinPer100g,",
            " fat_per_100g AS fatPer100g,",
            " carb_per_100g AS carbPer100g,",
            " unit_hint AS unitHint,",
            " is_builtin AS isBuiltin,",
            " deleted,",
            " status,",
            " create_time AS createTime,",
            " update_time AS updateTime",
            "FROM food_item",
            "WHERE id = #{id}",
            "  AND deleted = 0",
            "  AND status = 1",
            "LIMIT 1"
    })
    FoodItem findById(@Param("id") Long id);

    @Insert({
            "INSERT INTO food_item (",
            " user_id, food_name, category,",
            " calorie_per_100g, protein_per_100g, fat_per_100g, carb_per_100g,",
            " unit_hint, is_builtin, deleted, status",
            ") VALUES (",
            " #{userId}, #{foodName}, #{category},",
            " #{caloriePer100g}, #{proteinPer100g}, #{fatPer100g}, #{carbPer100g},",
            " #{unitHint}, #{isBuiltin}, #{deleted}, #{status}",
            ")"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FoodItem foodItem);

    @Select({
            "SELECT",
            " id,",
            " user_id AS userId,",
            " food_name AS foodName,",
            " category,",
            " calorie_per_100g AS caloriePer100g,",
            " protein_per_100g AS proteinPer100g,",
            " fat_per_100g AS fatPer100g,",
            " carb_per_100g AS carbPer100g,",
            " unit_hint AS unitHint,",
            " is_builtin AS isBuiltin,",
            " deleted,",
            " status",
            "FROM food_item",
            "WHERE deleted = 0",
            "  AND status = 1",
            "  AND is_builtin = 0",
            "  AND user_id = #{userId}",
            "  AND food_name = #{foodName}",
            "LIMIT 1"
    })
    FoodItem findUserCustomByName(@Param("userId") Long userId,
                                  @Param("foodName") String foodName);
}