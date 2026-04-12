package org.example.health.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.health.common.ApiResult;
import org.example.health.util.AdminAuthUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final JdbcTemplate jdbcTemplate;
    private final AdminAuthUtil adminAuthUtil;

    public AdminUserController(JdbcTemplate jdbcTemplate, AdminAuthUtil adminAuthUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.adminAuthUtil = adminAuthUtil;
    }

    @GetMapping
    public ApiResult list(
            HttpServletRequest request,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer role,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Long adminId = adminAuthUtil.requireAdmin(request);
        if (adminId == null) {
            return ApiResult.fail("无管理员权限");
        }

        StringBuilder where = new StringBuilder(" where deleted = 0 ");
        List<Object> params = new ArrayList<>();

        if (username != null && !username.isBlank()) {
            where.append(" and username like ? ");
            params.add("%" + username.trim() + "%");
        }
        if (phone != null && !phone.isBlank()) {
            where.append(" and phone like ? ");
            params.add("%" + phone.trim() + "%");
        }
        if (status != null) {
            where.append(" and status = ? ");
            params.add(status);
        }
        if (role != null) {
            where.append(" and role = ? ");
            params.add(role);
        }

        String countSql = "select count(*) from sys_user " + where;
        Long total = jdbcTemplate.queryForObject(countSql, params.toArray(), Long.class);

        int offset = (page - 1) * pageSize;
        String listSql = "select id, username, phone, nickname, gender, email, role, status, avatar_url, created_at " +
                "from sys_user " + where + " order by id desc limit ? offset ?";

        List<Object> listParams = new ArrayList<>(params);
        listParams.add(pageSize);
        listParams.add(offset);

        List<Map<String, Object>> list = jdbcTemplate.queryForList(listSql, listParams.toArray());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", total == null ? 0L : total);
        data.put("page", page);
        data.put("pageSize", pageSize);

        return ApiResult.success(data);
    }

    @PutMapping("/{id}/status")
    public ApiResult updateStatus(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        Long adminId = adminAuthUtil.requireAdmin(request);
        if (adminId == null) {
            return ApiResult.fail("无管理员权限");
        }

        Object statusObj = body.get("status");
        if (statusObj == null) {
            return ApiResult.fail("status不能为空");
        }

        int status = Integer.parseInt(String.valueOf(statusObj));
        if (status != 0 && status != 1) {
            return ApiResult.fail("status只能是0或1");
        }

        int rows = jdbcTemplate.update("update sys_user set status = ? where id = ? and deleted = 0", status, id);
        if (rows <= 0) {
            return ApiResult.fail("更新失败");
        }

        return ApiResult.success("操作成功");
    }

    @PutMapping("/{id}/role")
    public ApiResult updateRole(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        Long adminId = adminAuthUtil.requireAdmin(request);
        if (adminId == null) {
            return ApiResult.fail("无管理员权限");
        }

        if (Objects.equals(adminId, id)) {
            return ApiResult.fail("不能修改自己的管理员角色");
        }

        Object roleObj = body.get("role");
        if (roleObj == null) {
            return ApiResult.fail("role不能为空");
        }

        int role = Integer.parseInt(String.valueOf(roleObj));
        if (role != 0 && role != 1) {
            return ApiResult.fail("role只能是0或1");
        }

        int rows = jdbcTemplate.update("update sys_user set role = ? where id = ? and deleted = 0", role, id);
        if (rows <= 0) {
            return ApiResult.fail("更新失败");
        }

        return ApiResult.success("操作成功");
    }
}