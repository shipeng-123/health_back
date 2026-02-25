package org.example.health.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateFoodItemReq {

    @NotBlank(message = "食物名称不能为空")
    @Size(max = 100, message = "食物名称不能超过100个字符")
    private String foodName;

    @Size(max = 50, message = "分类不能超过50个字符")
    private String category;

    @NotNull(message = "每100g热量不能为空")
    @DecimalMin(value = "0.01", message = "每100g热量必须大于0")
    private BigDecimal caloriePer100g;

    @DecimalMin(value = "0.0", message = "蛋白质不能小于0")
    private BigDecimal proteinPer100g;

    @DecimalMin(value = "0.0", message = "脂肪不能小于0")
    private BigDecimal fatPer100g;

    @DecimalMin(value = "0.0", message = "碳水不能小于0")
    private BigDecimal carbPer100g;

    @Size(max = 20, message = "单位提示不能超过20个字符")
    private String unitHint;

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getCaloriePer100g() {
        return caloriePer100g;
    }

    public void setCaloriePer100g(BigDecimal caloriePer100g) {
        this.caloriePer100g = caloriePer100g;
    }

    public BigDecimal getProteinPer100g() {
        return proteinPer100g;
    }

    public void setProteinPer100g(BigDecimal proteinPer100g) {
        this.proteinPer100g = proteinPer100g;
    }

    public BigDecimal getFatPer100g() {
        return fatPer100g;
    }

    public void setFatPer100g(BigDecimal fatPer100g) {
        this.fatPer100g = fatPer100g;
    }

    public BigDecimal getCarbPer100g() {
        return carbPer100g;
    }

    public void setCarbPer100g(BigDecimal carbPer100g) {
        this.carbPer100g = carbPer100g;
    }

    public String getUnitHint() {
        return unitHint;
    }

    public void setUnitHint(String unitHint) {
        this.unitHint = unitHint;
    }
}