package org.example.health.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.health.common.ApiResult;
import org.example.health.util.AdminAuthUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/foods")
public class AdminFoodController {

    private final JdbcTemplate jdbcTemplate;
    private final AdminAuthUtil adminAuthUtil;

    public AdminFoodController(JdbcTemplate jdbcTemplate, AdminAuthUtil adminAuthUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.adminAuthUtil = adminAuthUtil;
    }

    @GetMapping
    public ApiResult list(
            HttpServletRequest request,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isBuiltin,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Long adminId = adminAuthUtil.requireAdmin(request);
        if (adminId == null) {
            return ApiResult.fail("无管理员权限");
        }

        StringBuilder where = new StringBuilder(" where f.deleted = 0 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            where.append(" and (f.food_name like ? or f.category like ?) ");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }

        if (status != null) {
            where.append(" and f.status = ? ");
            params.add(status);
        }

        if (isBuiltin != null) {
            where.append(" and f.is_builtin = ? ");
            params.add(isBuiltin);
        }

        Long total = jdbcTemplate.queryForObject(
                "select count(*) " +
                        "from food_item f " +
                        "left join sys_user u on f.user_id = u.id " +
                        where,
                params.toArray(),
                Long.class
        );

        int offset = (page - 1) * pageSize;
        List<Object> listParams = new ArrayList<>(params);
        listParams.add(pageSize);
        listParams.add(offset);

        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "select " +
                        "f.id, f.user_id, f.food_name, f.category, f.calorie_per_100g, " +
                        "f.protein_per_100g, f.fat_per_100g, f.carb_per_100g, " +
                        "f.unit_hint, f.is_builtin, f.status, f.create_time, " +
                        "u.username as creator_username, u.nickname as creator_nickname " +
                        "from food_item f " +
                        "left join sys_user u on f.user_id = u.id " +
                        where +
                        " order by f.id desc limit ? offset ?",
                listParams.toArray()
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", total == null ? 0L : total);
        data.put("page", page);
        data.put("pageSize", pageSize);

        return ApiResult.success(data);
    }

    @PostMapping
    public ApiResult create(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long adminId = adminAuthUtil.requireAdmin(request);
        if (adminId == null) {
            return ApiResult.fail("无管理员权限");
        }

        String foodName = str(body.get("foodName"));
        if (foodName == null || foodName.isBlank()) {
            return ApiResult.fail("foodName不能为空");
        }

        BigDecimal calorie = dec(body.get("caloriePer100g"));
        if (calorie == null || calorie.compareTo(BigDecimal.ZERO) <= 0) {
            return ApiResult.fail("caloriePer100g必须大于0");
        }

        String category = str(body.get("category"));
        BigDecimal protein = dec(body.get("proteinPer100g"));
        BigDecimal fat = dec(body.get("fatPer100g"));
        BigDecimal carb = dec(body.get("carbPer100g"));
        String unitHint = str(body.get("unitHint"));
        Integer status = body.get("status") == null ? 1 : Integer.parseInt(String.valueOf(body.get("status")));

        int rows = jdbcTemplate.update(
                "insert into food_item(" +
                        "user_id, food_name, category, calorie_per_100g, protein_per_100g, fat_per_100g, carb_per_100g, unit_hint, is_builtin, status, deleted" +
                        ") values(null, ?, ?, ?, ?, ?, ?, ?, 1, ?, 0)",
                foodName,
                category,
                calorie,
                protein == null ? BigDecimal.ZERO : protein,
                fat == null ? BigDecimal.ZERO : fat,
                carb == null ? BigDecimal.ZERO : carb,
                unitHint == null || unitHint.isBlank() ? "g" : unitHint,
                status
        );

        if (rows <= 0) {
            return ApiResult.fail("新增失败");
        }

        return ApiResult.success("新增成功");
    }

    @PutMapping("/{id}")
    public ApiResult update(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        Long adminId = adminAuthUtil.requireAdmin(request);
        if (adminId == null) {
            return ApiResult.fail("无管理员权限");
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select id, is_builtin from food_item where id = ? and deleted = 0",
                id
        );
        if (rows.isEmpty()) {
            return ApiResult.fail("食物不存在");
        }

        Integer isBuiltin = rows.get(0).get("is_builtin") == null
                ? 0
                : Integer.parseInt(String.valueOf(rows.get(0).get("is_builtin")));

        if (isBuiltin != 1) {
            return ApiResult.fail("用户自定义食物不支持直接编辑，请先转为系统食物");
        }

        String foodName = str(body.get("foodName"));
        if (foodName == null || foodName.isBlank()) {
            return ApiResult.fail("foodName不能为空");
        }

        BigDecimal calorie = dec(body.get("caloriePer100g"));
        if (calorie == null || calorie.compareTo(BigDecimal.ZERO) <= 0) {
            return ApiResult.fail("caloriePer100g必须大于0");
        }

        String category = str(body.get("category"));
        BigDecimal protein = dec(body.get("proteinPer100g"));
        BigDecimal fat = dec(body.get("fatPer100g"));
        BigDecimal carb = dec(body.get("carbPer100g"));
        String unitHint = str(body.get("unitHint"));
        Integer status = body.get("status") == null ? 1 : Integer.parseInt(String.valueOf(body.get("status")));

        int updated = jdbcTemplate.update(
                "update food_item set " +
                        "food_name = ?, category = ?, calorie_per_100g = ?, protein_per_100g = ?, fat_per_100g = ?, carb_per_100g = ?, unit_hint = ?, status = ? " +
                        "where id = ? and deleted = 0",
                foodName,
                category,
                calorie,
                protein == null ? BigDecimal.ZERO : protein,
                fat == null ? BigDecimal.ZERO : fat,
                carb == null ? BigDecimal.ZERO : carb,
                unitHint == null || unitHint.isBlank() ? "g" : unitHint,
                status,
                id
        );

        if (updated <= 0) {
            return ApiResult.fail("更新失败");
        }

        return ApiResult.success("更新成功");
    }

    @PutMapping("/{id}/promote")
    public ApiResult promoteToBuiltin(HttpServletRequest request, @PathVariable Long id) {
        Long adminId = adminAuthUtil.requireAdmin(request);
        if (adminId == null) {
            return ApiResult.fail("无管理员权限");
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select id, is_builtin from food_item where id = ? and deleted = 0",
                id
        );
        if (rows.isEmpty()) {
            return ApiResult.fail("食物不存在");
        }

        Integer isBuiltin = rows.get(0).get("is_builtin") == null
                ? 0
                : Integer.parseInt(String.valueOf(rows.get(0).get("is_builtin")));

        if (isBuiltin == 1) {
            return ApiResult.fail("该食物已经是系统食物");
        }

        int updated = jdbcTemplate.update(
                "update food_item set is_builtin = 1, user_id = null where id = ? and deleted = 0",
                id
        );
        if (updated <= 0) {
            return ApiResult.fail("转为系统食物失败");
        }

        return ApiResult.success("已转为系统食物");
    }

    @DeleteMapping("/{id}")
    public ApiResult delete(HttpServletRequest request, @PathVariable Long id) {
        Long adminId = adminAuthUtil.requireAdmin(request);
        if (adminId == null) {
            return ApiResult.fail("无管理员权限");
        }

        int rows = jdbcTemplate.update(
                "update food_item set deleted = 1 where id = ? and deleted = 0",
                id
        );
        if (rows <= 0) {
            return ApiResult.fail("删除失败");
        }

        return ApiResult.success("删除成功");
    }

    private String str(Object v) {
        return v == null ? null : String.valueOf(v).trim();
    }

    private BigDecimal dec(Object v) {
        if (v == null || String.valueOf(v).isBlank()) {
            return null;
        }
        return new BigDecimal(String.valueOf(v));
    }
}