package org.example.health.controller;

import jakarta.validation.Valid;
import org.example.health.common.ApiResult;
import org.example.health.dto.CreateFoodItemReq;
import org.example.health.dto.FoodItemResp;
import org.example.health.entity.FoodItem;
import org.example.health.mapper.FoodItemMapper;
import org.springframework.web.bind.annotation.*;
import org.example.health.util.JwtUtil;
import java.util.List;

@RestController
@RequestMapping("/api/food")
public class FoodController {

    private final FoodItemMapper foodItemMapper;

    public FoodController(FoodItemMapper foodItemMapper) {
        this.foodItemMapper = foodItemMapper;
    }

    // TODO: 后续接JWT后，从token解析真实用户ID
    private Long getCurrentUserId(String authorization) {
        Long uid = JwtUtil.getUserIdFromHeader(authorization);
        if (uid == null) {
            throw new RuntimeException("未登录或token无效");
        }
        return uid;
    }

    @GetMapping("/search")
    public ApiResult<List<FoodItemResp>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long userId = getCurrentUserId(authorization);

        if (limit == null || limit <= 0) limit = 20;
        if (limit > 100) limit = 100;

        List<FoodItemResp> list = foodItemMapper.search(userId, keyword, limit);
        return ApiResult.success(list);
    }

    @PostMapping("/custom")
    public ApiResult<Object> createCustomFood(
            @Valid @RequestBody CreateFoodItemReq req,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long userId = getCurrentUserId(authorization);

        String foodName = req.getFoodName() == null ? null : req.getFoodName().trim();
        if (foodName == null || foodName.isEmpty()) {
            return ApiResult.fail("食物名称不能为空");
        }

        FoodItem existed = foodItemMapper.findUserCustomByName(userId, foodName);
        if (existed != null) {
            return ApiResult.fail("你已创建同名食物，请直接搜索使用");
        }

        FoodItem food = new FoodItem();
        food.setUserId(userId);
        food.setFoodName(foodName);
        food.setCategory(req.getCategory() == null ? null : req.getCategory().trim());
        food.setCaloriePer100g(req.getCaloriePer100g());
        food.setProteinPer100g(req.getProteinPer100g());
        food.setFatPer100g(req.getFatPer100g());
        food.setCarbPer100g(req.getCarbPer100g());
        food.setUnitHint((req.getUnitHint() == null || req.getUnitHint().trim().isEmpty()) ? "g" : req.getUnitHint().trim());
        food.setIsBuiltin(0);
        food.setDeleted(0);
        food.setStatus(1);

        foodItemMapper.insert(food);
        return ApiResult.success(food.getId());
    }
}