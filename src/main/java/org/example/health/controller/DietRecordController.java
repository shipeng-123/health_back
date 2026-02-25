package org.example.health.controller;

import jakarta.validation.Valid;
import org.example.health.common.ApiResult;
import org.example.health.dto.CreateDietRecordReq;
import org.example.health.dto.DietRecordDateResp;
import org.example.health.dto.DietRecordItemResp;
import org.example.health.entity.DietRecord;
import org.example.health.entity.FoodItem;
import org.example.health.mapper.DietRecordMapper;
import org.example.health.mapper.FoodItemMapper;
import org.example.health.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/diet-record")
public class DietRecordController {

    private final DietRecordMapper dietRecordMapper;
    private final FoodItemMapper foodItemMapper;

    public DietRecordController(DietRecordMapper dietRecordMapper, FoodItemMapper foodItemMapper) {
        this.dietRecordMapper = dietRecordMapper;
        this.foodItemMapper = foodItemMapper;
    }

    // ✅ 从 token 解析用户ID（不再写死 1）
    private Long getCurrentUserId(String authorization) {
        Long uid = JwtUtil.getUserIdFromHeader(authorization);
        if (uid == null) {
            throw new RuntimeException("未登录或token无效");
        }
        return uid;
    }

    @PostMapping
    public ApiResult<Object> create(@Valid @RequestBody CreateDietRecordReq req,
                                    @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = getCurrentUserId(authorization);

        FoodItem food = foodItemMapper.findById(req.getFoodItemId());
        if (food == null) {
            return ApiResult.fail("食物不存在或已停用");
        }

        BigDecimal intakeGram = BigDecimal.valueOf(req.getIntakeGram());
        BigDecimal caloriePer100g = food.getCaloriePer100g();

        // 总热量 = 每100g热量 * 摄入克数 / 100
        BigDecimal totalCalories = caloriePer100g
                .multiply(intakeGram)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        DietRecord record = new DietRecord();
        record.setUserId(userId);
        record.setFoodItemId(req.getFoodItemId());
        record.setMealType(req.getMealType());
        record.setIntakeGram(intakeGram);
        record.setCaloriePer100g(caloriePer100g);
        record.setTotalCalories(totalCalories);
        record.setRecordDate(req.getRecordDate());
        record.setRemark(req.getRemark());

        dietRecordMapper.insert(record);

        return ApiResult.success(record.getId());
    }

    @GetMapping("/date")
    public ApiResult<DietRecordDateResp> getByDate(
            @RequestParam LocalDate date,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long userId = getCurrentUserId(authorization);

        int count = dietRecordMapper.countByUserAndDate(userId, date);
        BigDecimal totalCalories = dietRecordMapper.sumCaloriesByUserAndDate(userId, date);
        if (totalCalories == null) totalCalories = BigDecimal.ZERO;

        List<DietRecordItemResp> items = dietRecordMapper.findItemsByUserAndDate(userId, date);

        DietRecordDateResp resp = new DietRecordDateResp();
        resp.setDate(date);
        resp.setRecordCount(count);
        resp.setTotalCalories(totalCalories);
        resp.setRecords(items);

        return ApiResult.success(resp);
    }
}