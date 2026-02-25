package org.example.health.dto;

import lombok.Data;

@Data
public class FoodItemResp {
    private Long id;
    private String foodName;
    private String category;
    private Double caloriePer100g;
    private Double proteinPer100g;
    private Double fatPer100g;
    private Double carbPer100g;
    private Integer isBuiltin;
    private Long userId; // 可选
}