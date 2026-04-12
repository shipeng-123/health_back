package org.example.health.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.health.common.ApiResult;
import org.example.health.util.AdminAuthUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@RestController
@RequestMapping("/api/admin/plan-templates")
public class AdminPlanTemplateController {

    private final JdbcTemplate jdbcTemplate;
    private final AdminAuthUtil adminAuthUtil;

    public AdminPlanTemplateController(JdbcTemplate jdbcTemplate, AdminAuthUtil adminAuthUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.adminAuthUtil = adminAuthUtil;
    }

    @GetMapping
    public ApiResult list(
            HttpServletRequest request,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        Long adminId = adminAuthUtil.requireAdmin(request);
        if (adminId == null) {
            return ApiResult.fail("无管理员权限");
        }

        StringBuilder where = new StringBuilder(" where 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            where.append(" and (name like ? or description like ?) ");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }
        if (status != null) {
            where.append(" and status = ? ");
            params.add(status);
        }

        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "select id, name, description, status, create_time, update_time from sport_plan_template " + where + " order by id desc",
                params.toArray()
        );

        return ApiResult.success(list);
    }

    @GetMapping("/{id}")
    public ApiResult detail(HttpServletRequest request, @PathVariable Long id) {
        Long adminId = adminAuthUtil.requireAdmin(request);
        if (adminId == null) {
            return ApiResult.fail("无管理员权限");
        }

        List<Map<String, Object>> templates = jdbcTemplate.queryForList(
                "select id, name, description, status, create_time, update_time from sport_plan_template where id = ?",
                id
        );
        if (templates.isEmpty()) {
            return ApiResult.fail("模板不存在");
        }

        Map<String, Object> data = new LinkedHashMap<>(templates.get(0));
        List<Map<String, Object>> items = jdbcTemplate.queryForList(
                "select id, template_id, day_of_week, sport_type, target_duration_min, target_distance_km, remind_time, remark, sort_no " +
                        "from sport_plan_template_item where template_id = ? order by day_of_week asc, sort_no asc, id asc",
                id
        );
        data.put("items", items);

        return ApiResult.success(data);
    }

    @PostMapping
    public ApiResult create(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long adminId = adminAuthUtil.requireAdmin(request);
        if (adminId == null) {
            return ApiResult.fail("无管理员权限");
        }

        String name = str(body.get("name"));
        if (name == null || name.isBlank()) {
            return ApiResult.fail("name不能为空");
        }
        String description = str(body.get("description"));
        Integer status = body.get("status") == null ? 1 : Integer.parseInt(String.valueOf(body.get("status")));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "insert into sport_plan_template(name, description, status) values(?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, status);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            return ApiResult.fail("新增模板失败");
        }

        Long templateId = key.longValue();
        saveItems(templateId, body.get("items"));

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

        String name = str(body.get("name"));
        if (name == null || name.isBlank()) {
            return ApiResult.fail("name不能为空");
        }
        String description = str(body.get("description"));
        Integer status = body.get("status") == null ? 1 : Integer.parseInt(String.valueOf(body.get("status")));

        int rows = jdbcTemplate.update(
                "update sport_plan_template set name = ?, description = ?, status = ? where id = ?",
                name, description, status, id
        );
        if (rows <= 0) {
            return ApiResult.fail("更新失败");
        }

        jdbcTemplate.update("delete from sport_plan_template_item where template_id = ?", id);
        saveItems(id, body.get("items"));

        return ApiResult.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResult delete(HttpServletRequest request, @PathVariable Long id) {
        Long adminId = adminAuthUtil.requireAdmin(request);
        if (adminId == null) {
            return ApiResult.fail("无管理员权限");
        }

        jdbcTemplate.update("delete from sport_plan_template_item where template_id = ?", id);
        int rows = jdbcTemplate.update("delete from sport_plan_template where id = ?", id);
        if (rows <= 0) {
            return ApiResult.fail("删除失败");
        }

        return ApiResult.success("删除成功");
    }

    @SuppressWarnings("unchecked")
    private void saveItems(Long templateId, Object itemsObj) {
        if (!(itemsObj instanceof List<?> items)) {
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            Object itemObj = items.get(i);
            if (!(itemObj instanceof Map<?, ?> rawMap)) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                item.put(String.valueOf(entry.getKey()), entry.getValue());
            }

            Integer dayOfWeek = intVal(item.get("dayOfWeek"));
            String sportType = str(item.get("sportType"));
            Integer targetDurationMin = intVal(item.get("targetDurationMin"));
            BigDecimal targetDistanceKm = dec(item.get("targetDistanceKm"));
            String remindTime = str(item.get("remindTime"));
            String remark = str(item.get("remark"));
            Integer sortNo = item.get("sortNo") == null ? i + 1 : intVal(item.get("sortNo"));

            if (dayOfWeek == null || sportType == null || sportType.isBlank() || targetDurationMin == null) {
                continue;
            }

            jdbcTemplate.update(
                    "insert into sport_plan_template_item(template_id, day_of_week, sport_type, target_duration_min, target_distance_km, remind_time, remark, sort_no) " +
                            "values(?, ?, ?, ?, ?, ?, ?, ?)",
                    templateId, dayOfWeek, sportType, targetDurationMin, targetDistanceKm, remindTime, remark, sortNo
            );
        }
    }

    private String str(Object v) {
        return v == null ? null : String.valueOf(v).trim();
    }

    private Integer intVal(Object v) {
        if (v == null || String.valueOf(v).isBlank()) {
            return null;
        }
        return Integer.parseInt(String.valueOf(v));
    }

    private BigDecimal dec(Object v) {
        if (v == null || String.valueOf(v).isBlank()) {
            return null;
        }
        return new BigDecimal(String.valueOf(v));
    }
}