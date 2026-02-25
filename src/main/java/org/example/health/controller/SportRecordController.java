package org.example.health.controller;

import jakarta.validation.Valid;
import org.example.health.common.ApiResult;
import org.example.health.dto.CreateSportRecordReq;
import org.example.health.entity.SportRecord;
import org.example.health.entity.SysUser;
import org.example.health.mapper.SportRecordMapper;
import org.example.health.mapper.SysUserMapper;
import org.example.health.util.JwtUtil;
import org.example.health.util.SportMetUtil;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sport-record")
public class SportRecordController {

    private final SportRecordMapper sportRecordMapper;
    private final SysUserMapper sysUserMapper;

    public SportRecordController(SportRecordMapper sportRecordMapper, SysUserMapper sysUserMapper) {
        this.sportRecordMapper = sportRecordMapper;
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 新增运动记录（后端自动计算热量）
     */
    @PostMapping
    public ApiResult<Map<String, Object>> createRecord(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateSportRecordReq req
    ) {
        // 1) 从 token 解析 userId
        Long userId = getUserIdFromAuthHeader(authorization);
        if (userId == null) {
            return ApiResult.fail("未登录或token无效");
        }

        // 2) 查询用户
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return ApiResult.fail("用户不存在");
        }

        // 3) 校验运动类型
        String sportType = req.getSportType();
        if (!SportMetUtil.contains(sportType)) {
            return ApiResult.fail("不支持的运动类型：" + sportType);
        }

        // 4) 获取用户体重（SysUser 里需要有 weight 字段）
        BigDecimal weight = user.getWeight();
        if (weight == null) {
            return ApiResult.fail("请先完善个人资料中的体重信息");
        }
        if (weight.compareTo(BigDecimal.ZERO) <= 0) {
            return ApiResult.fail("体重必须大于0");
        }

        // 5) 计算热量：calories = MET × 体重(kg) × 时长(min) / 60
        BigDecimal met = SportMetUtil.getMet(sportType);
        BigDecimal durationMin = new BigDecimal(req.getDurationMin());

        BigDecimal calories = met.multiply(weight)
                .multiply(durationMin)
                .divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);

        // 6) 保存
        SportRecord record = new SportRecord();
        record.setUserId(userId);
        record.setSportType(sportType);
        record.setDurationMin(req.getDurationMin());
        record.setDistanceKm(req.getDistanceKm());
        record.setMetValue(met);
        record.setCalories(calories);
        record.setRecordDate(req.getRecordDate());
        record.setRemark(req.getRemark());

        int rows = sportRecordMapper.insert(record);
        if (rows <= 0) {
            return ApiResult.fail("保存失败，请稍后重试");
        }

        // 7) 返回结果（前端可直接显示）
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", record.getId());
        data.put("sportType", record.getSportType());
        data.put("durationMin", record.getDurationMin());
        data.put("metValue", record.getMetValue());
        data.put("calories", record.getCalories());
        data.put("recordDate", record.getRecordDate());

        return ApiResult.success(data);
    }

    // ----------------- 工具方法：与你 UserController 保持一致 -----------------

    /**
     * 从 Authorization: Bearer xxx 里解析 userId
     */
    private Long getUserIdFromAuthHeader(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            System.out.println("authorization is null/blank");
            return null;
        }

        String token = authorization.trim();
        System.out.println("raw authorization = [" + token + "]");

        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        System.out.println("token before parse = [" + token + "]");

        try {
            Long userId = JwtUtil.parseUserId(token);
            System.out.println("parsed userId = " + userId);
            return userId;
        } catch (Exception e) {
            System.out.println("JWT parse error:");
            e.printStackTrace();
            return null;
        }
    }
    @GetMapping("/date")
    public ApiResult<org.example.health.dto.SportRecordDateResp> getRecordsByDate(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("date") java.time.LocalDate date
    ) {
        Long userId = getUserIdFromAuthHeader(authorization);
        if (userId == null) {
            return ApiResult.fail("未登录或token无效");
        }

        // 查询当天记录（仅当前用户）
        java.util.List<org.example.health.entity.SportRecord> list =
                sportRecordMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.example.health.entity.SportRecord>()
                                .eq(org.example.health.entity.SportRecord::getUserId, userId)
                                .eq(org.example.health.entity.SportRecord::getRecordDate, date)
                                .orderByDesc(org.example.health.entity.SportRecord::getId)
                );

        java.util.List<org.example.health.dto.SportRecordItemResp> items = new java.util.ArrayList<>();
        int totalDuration = 0;
        java.math.BigDecimal totalCalories = java.math.BigDecimal.ZERO;

        if (list != null) {
            for (org.example.health.entity.SportRecord r : list) {
                org.example.health.dto.SportRecordItemResp item = new org.example.health.dto.SportRecordItemResp();
                item.setId(r.getId());
                item.setSportType(r.getSportType());
                item.setDurationMin(r.getDurationMin());
                item.setDistanceKm(r.getDistanceKm());
                item.setMetValue(r.getMetValue());
                item.setCalories(r.getCalories());
                item.setRecordDate(r.getRecordDate());
                item.setRemark(r.getRemark());
                item.setCreateTime(r.getCreateTime());
                items.add(item);

                if (r.getDurationMin() != null) {
                    totalDuration += r.getDurationMin();
                }
                if (r.getCalories() != null) {
                    totalCalories = totalCalories.add(r.getCalories());
                }
            }
        }

        totalCalories = totalCalories.setScale(2, java.math.RoundingMode.HALF_UP);

        org.example.health.dto.SportRecordDateResp resp = new org.example.health.dto.SportRecordDateResp();
        resp.setDate(date);
        resp.setRecordCount(items.size());
        resp.setTotalDurationMin(totalDuration);
        resp.setTotalCalories(totalCalories);
        resp.setRecords(items);

        return ApiResult.success(resp);
    }
    @GetMapping("/calendar")
    public ApiResult<org.example.health.dto.SportRecordCalendarResp> getCalendarSummary(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month
    ) {
        Long userId = getUserIdFromAuthHeader(authorization);
        if (userId == null) {
            return ApiResult.fail("未登录或token无效");
        }

        // 基本参数校验
        if (year == null || year < 2000 || year > 2100) {
            return ApiResult.fail("year参数不合法");
        }
        if (month == null || month < 1 || month > 12) {
            return ApiResult.fail("month参数不合法");
        }

        java.time.LocalDate startDate;
        try {
            startDate = java.time.LocalDate.of(year, month, 1);
        } catch (Exception e) {
            return ApiResult.fail("年月参数不合法");
        }
        java.time.LocalDate endDate = startDate.plusMonths(1);

        // 查询当月记录（当前用户）
        java.util.List<org.example.health.entity.SportRecord> list =
                sportRecordMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.example.health.entity.SportRecord>()
                                .eq(org.example.health.entity.SportRecord::getUserId, userId)
                                .ge(org.example.health.entity.SportRecord::getRecordDate, startDate)
                                .lt(org.example.health.entity.SportRecord::getRecordDate, endDate)
                                .orderByAsc(org.example.health.entity.SportRecord::getRecordDate)
                                .orderByAsc(org.example.health.entity.SportRecord::getId)
                );

        // 按日期聚合
        class DayAgg {
            int count = 0;
            int totalDuration = 0;
            java.math.BigDecimal totalCalories = java.math.BigDecimal.ZERO;
        }

        java.util.Map<java.time.LocalDate, DayAgg> aggMap = new java.util.LinkedHashMap<>();

        if (list != null) {
            for (org.example.health.entity.SportRecord r : list) {
                java.time.LocalDate d = r.getRecordDate();
                if (d == null) continue;

                DayAgg agg = aggMap.computeIfAbsent(d, k -> new DayAgg());
                agg.count++;

                if (r.getDurationMin() != null) {
                    agg.totalDuration += r.getDurationMin();
                }
                if (r.getCalories() != null) {
                    agg.totalCalories = agg.totalCalories.add(r.getCalories());
                }
            }
        }

        java.util.List<org.example.health.dto.SportRecordCalendarDayResp> days = new java.util.ArrayList<>();
        for (java.util.Map.Entry<java.time.LocalDate, DayAgg> e : aggMap.entrySet()) {
            org.example.health.dto.SportRecordCalendarDayResp item = new org.example.health.dto.SportRecordCalendarDayResp();
            item.setDate(e.getKey());
            item.setRecordCount(e.getValue().count);
            item.setTotalDurationMin(e.getValue().totalDuration);
            item.setTotalCalories(e.getValue().totalCalories.setScale(2, java.math.RoundingMode.HALF_UP));
            days.add(item);
        }

        org.example.health.dto.SportRecordCalendarResp resp = new org.example.health.dto.SportRecordCalendarResp();
        resp.setYear(year);
        resp.setMonth(month);
        resp.setDays(days);

        return ApiResult.success(resp);
    }
}