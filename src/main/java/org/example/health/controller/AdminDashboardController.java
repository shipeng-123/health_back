package org.example.health.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.health.common.ApiResult;
import org.example.health.util.AdminAuthUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final JdbcTemplate jdbcTemplate;
    private final AdminAuthUtil adminAuthUtil;

    public AdminDashboardController(JdbcTemplate jdbcTemplate, AdminAuthUtil adminAuthUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.adminAuthUtil = adminAuthUtil;
    }

    @GetMapping("/summary")
    public ApiResult summary(HttpServletRequest request) {
        Long adminId = adminAuthUtil.requireAdmin(request);
        if (adminId == null) {
            return ApiResult.fail("无管理员权限");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userCount", queryLong("select count(*) from sys_user where deleted = 0"));
        data.put("todayNewUserCount", queryLong("select count(*) from sys_user where deleted = 0 and date(created_at) = curdate()"));
        data.put("foodCount", queryLong("select count(*) from food_item where deleted = 0"));
        data.put("planTemplateCount", queryLong("select count(*) from sport_plan_template"));
        data.put("dietRecordCount", queryLong("select count(*) from diet_record"));
        data.put("sportRecordCount", queryLong("select count(*) from sport_record"));

        return ApiResult.success(data);
    }

    private Long queryLong(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }
}