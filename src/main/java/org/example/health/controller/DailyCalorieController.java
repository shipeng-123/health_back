package org.example.health.controller;

import org.example.health.common.ApiResult;
import org.example.health.dto.DailyCalorieSummaryResp;
import org.example.health.entity.SysUser;
import org.example.health.mapper.DietRecordMapper;
import org.example.health.mapper.SportRecordMapper;
import org.example.health.mapper.SysUserMapper;
import org.example.health.util.CalorieRecommendUtil;
import org.springframework.web.bind.annotation.*;
import org.example.health.util.JwtUtil;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/daily-calorie")
public class DailyCalorieController {

    private final DietRecordMapper dietRecordMapper;
    private final SportRecordMapper sportRecordMapper;
    private final SysUserMapper sysUserMapper;

    public DailyCalorieController(DietRecordMapper dietRecordMapper,
                                  SportRecordMapper sportRecordMapper,
                                  SysUserMapper sysUserMapper) {
        this.dietRecordMapper = dietRecordMapper;
        this.sportRecordMapper = sportRecordMapper;
        this.sysUserMapper = sysUserMapper;
    }

    // TODO：后续JWT从token取
    private Long getCurrentUserId(String authorization) {
        Long uid = JwtUtil.getUserIdFromHeader(authorization);
        if (uid == null) {
            throw new RuntimeException("未登录或token无效");
        }
        return uid;
    }
    @GetMapping("/summary")
    public ApiResult<DailyCalorieSummaryResp> summary(
            @RequestParam LocalDate date,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long userId = getCurrentUserId(authorization);

        BigDecimal intake = dietRecordMapper.sumCaloriesByUserAndDate(userId, date);
        BigDecimal sport = sportRecordMapper.sumCaloriesByUserAndDate(userId, date);
        if (intake == null) intake = BigDecimal.ZERO;
        if (sport == null) sport = BigDecimal.ZERO;

        BigDecimal net = intake.subtract(sport);

        SysUser user = sysUserMapper.selectById(userId);
        BigDecimal recommended = CalorieRecommendUtil.calcRecommended(user);

        DailyCalorieSummaryResp resp = new DailyCalorieSummaryResp();
        resp.setDate(date);
        resp.setIntakeCalories(intake);
        resp.setSportCalories(sport);
        resp.setNetCalories(net);

        if (recommended == null) {
            resp.setProfileReady(false);
            resp.setProfileTip("用户资料不完整：请完善身高、体重、出生日期、活动水平、目标类型");
            resp.setRecommendedCalories(null);
            resp.setDiffVsRecommended(null);
        } else {
            resp.setProfileReady(true);
            resp.setProfileTip(null);
            resp.setRecommendedCalories(recommended);
            resp.setDiffVsRecommended(net.subtract(recommended));
        }

        return ApiResult.success(resp);
    }
}