package org.example.health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import org.example.health.common.ApiResult;
import org.example.health.dto.BodyMetricResp;
import org.example.health.dto.BodyMetricTrendResp;
import org.example.health.dto.UpsertBodyMetricReq;
import org.example.health.entity.BodyMetricRecord;
import org.example.health.entity.SysUser;
import org.example.health.mapper.BodyMetricRecordMapper;
import org.example.health.mapper.SysUserMapper;
import org.example.health.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/body-metric")
public class BodyMetricController {

    private final BodyMetricRecordMapper bodyMetricRecordMapper;
    private final SysUserMapper sysUserMapper;

    public BodyMetricController(BodyMetricRecordMapper bodyMetricRecordMapper, SysUserMapper sysUserMapper) {
        this.bodyMetricRecordMapper = bodyMetricRecordMapper;
        this.sysUserMapper = sysUserMapper;
    }

    // 1) 新增/覆盖（同一天一条）
    @PostMapping
    public ApiResult<BodyMetricResp> upsert(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpsertBodyMetricReq req
    ) {
        Long userId = JwtUtil.getUserIdFromHeader(authorization);
        if (userId == null) return ApiResult.fail("未登录或token无效");

        // 查用户（用于BMI：需要身高）
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) return ApiResult.fail("用户不存在");

        // 同一天是否已有记录：有则更新，无则新增
        BodyMetricRecord exist = bodyMetricRecordMapper.selectOne(
                new LambdaQueryWrapper<BodyMetricRecord>()
                        .eq(BodyMetricRecord::getUserId, userId)
                        .eq(BodyMetricRecord::getRecordDate, req.getRecordDate())
                        .last("limit 1")
        );

        BodyMetricRecord r = (exist != null) ? exist : new BodyMetricRecord();
        r.setUserId(userId);
        r.setRecordDate(req.getRecordDate());
        r.setWeightKg(req.getWeightKg());
        r.setBodyFatPct(req.getBodyFatPct());
        r.setChestCm(req.getChestCm());
        r.setWaistCm(req.getWaistCm());
        r.setHipCm(req.getHipCm());
        r.setRemark(req.getRemark());

        if (exist == null) {
            int rows = bodyMetricRecordMapper.insert(r);
            if (rows <= 0) return ApiResult.fail("保存失败，请稍后重试");
        } else {
            int rows = bodyMetricRecordMapper.updateById(r);
            if (rows <= 0) return ApiResult.fail("更新失败，请稍后重试");
        }

        return ApiResult.success(toResp(user, r));
    }

    // 2) 最新一条（用于首页展示 BMI + 评估）
    @GetMapping("/latest")
    public ApiResult<BodyMetricResp> latest(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long userId = JwtUtil.getUserIdFromHeader(authorization);
        if (userId == null) return ApiResult.fail("未登录或token无效");

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) return ApiResult.fail("用户不存在");

        BodyMetricRecord r = bodyMetricRecordMapper.selectOne(
                new LambdaQueryWrapper<BodyMetricRecord>()
                        .eq(BodyMetricRecord::getUserId, userId)
                        .orderByDesc(BodyMetricRecord::getRecordDate)
                        .orderByDesc(BodyMetricRecord::getId)
                        .last("limit 1")
        );

        if (r == null) {
            return ApiResult.fail("暂无身体指标记录，请先新增一次记录");
        }

        return ApiResult.success(toResp(user, r));
    }

    // 3) 查某天
    @GetMapping("/date")
    public ApiResult<BodyMetricResp> byDate(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("date") LocalDate date
    ) {
        Long userId = JwtUtil.getUserIdFromHeader(authorization);
        if (userId == null) return ApiResult.fail("未登录或token无效");

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) return ApiResult.fail("用户不存在");

        BodyMetricRecord r = bodyMetricRecordMapper.selectOne(
                new LambdaQueryWrapper<BodyMetricRecord>()
                        .eq(BodyMetricRecord::getUserId, userId)
                        .eq(BodyMetricRecord::getRecordDate, date)
                        .last("limit 1")
        );

        if (r == null) return ApiResult.fail("该日期无记录");
        return ApiResult.success(toResp(user, r));
    }

    // 4) 趋势（给ECharts折线图）
    @GetMapping("/trend")
    public ApiResult<BodyMetricTrendResp> trend(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("start") LocalDate start,
            @RequestParam("end") LocalDate end
    ) {
        Long userId = JwtUtil.getUserIdFromHeader(authorization);
        if (userId == null) return ApiResult.fail("未登录或token无效");
        if (start.isAfter(end)) return ApiResult.fail("start不能大于end");

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) return ApiResult.fail("用户不存在");

        List<BodyMetricRecord> list = bodyMetricRecordMapper.selectList(
                new LambdaQueryWrapper<BodyMetricRecord>()
                        .eq(BodyMetricRecord::getUserId, userId)
                        .ge(BodyMetricRecord::getRecordDate, start)
                        .le(BodyMetricRecord::getRecordDate, end)
                        .orderByAsc(BodyMetricRecord::getRecordDate)
                        .orderByAsc(BodyMetricRecord::getId)
        );

        BodyMetricTrendResp resp = new BodyMetricTrendResp();
        List<LocalDate> dates = new ArrayList<>();
        List<BigDecimal> weightKg = new ArrayList<>();
        List<BigDecimal> bodyFatPct = new ArrayList<>();
        List<BigDecimal> waistCm = new ArrayList<>();
        List<BigDecimal> hipCm = new ArrayList<>();
        List<BigDecimal> chestCm = new ArrayList<>();
        List<BigDecimal> bmi = new ArrayList<>();

        if (list != null) {
            for (BodyMetricRecord r : list) {
                dates.add(r.getRecordDate());
                weightKg.add(r.getWeightKg());
                bodyFatPct.add(r.getBodyFatPct());
                waistCm.add(r.getWaistCm());
                hipCm.add(r.getHipCm());
                chestCm.add(r.getChestCm());
                bmi.add(calcBmi(user, r.getWeightKg())); // 可能为空
            }
        }

        resp.setDates(dates);
        resp.setWeightKg(weightKg);
        resp.setBodyFatPct(bodyFatPct);
        resp.setWaistCm(waistCm);
        resp.setHipCm(hipCm);
        resp.setChestCm(chestCm);
        resp.setBmi(bmi);

        return ApiResult.success(resp);
    }

    // 5) 删除（可选）
    @DeleteMapping("/{id}")
    public ApiResult<String> delete(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("id") Long id
    ) {
        Long userId = JwtUtil.getUserIdFromHeader(authorization);
        if (userId == null) return ApiResult.fail("未登录或token无效");

        BodyMetricRecord r = bodyMetricRecordMapper.selectById(id);
        if (r == null) return ApiResult.fail("记录不存在");
        if (!userId.equals(r.getUserId())) return ApiResult.fail("无权限");

        int rows = bodyMetricRecordMapper.deleteById(id);
        if (rows <= 0) return ApiResult.fail("删除失败");
        return ApiResult.success("ok");
    }

    // ----------------- 内部：BMI计算与评估 -----------------

    private BodyMetricResp toResp(SysUser user, BodyMetricRecord r) {
        BodyMetricResp resp = new BodyMetricResp();
        resp.setId(r.getId());
        resp.setRecordDate(r.getRecordDate());
        resp.setWeightKg(r.getWeightKg());
        resp.setBodyFatPct(r.getBodyFatPct());
        resp.setChestCm(r.getChestCm());
        resp.setWaistCm(r.getWaistCm());
        resp.setHipCm(r.getHipCm());

        BigDecimal bmi = calcBmi(user, r.getWeightKg());
        resp.setBmi(bmi);

        if (bmi != null) {
            String level = bmiLevel(bmi);
            resp.setBmiLevel(level);
            resp.setBmiAdvice(bmiAdvice(level));
        }

        return resp;
    }

    private BigDecimal calcBmi(SysUser user, BigDecimal weightKg) {
        if (weightKg == null) return null;
        if (user.getHeightCm() == null) return null; // 没身高就不算BMI
        if (user.getHeightCm().compareTo(BigDecimal.ZERO) <= 0) return null;

        BigDecimal heightM = user.getHeightCm().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal heightM2 = heightM.multiply(heightM);

        if (heightM2.compareTo(BigDecimal.ZERO) <= 0) return null;

        return weightKg.divide(heightM2, 2, RoundingMode.HALF_UP);
    }

    private String bmiLevel(BigDecimal bmi) {
        // 这里用通用分类：你想按“中国标准”也可以后面再改
        if (bmi.compareTo(new BigDecimal("18.5")) < 0) return "偏瘦";
        if (bmi.compareTo(new BigDecimal("24.0")) < 0) return "正常";
        if (bmi.compareTo(new BigDecimal("28.0")) < 0) return "超重";
        return "肥胖";
    }

    private String bmiAdvice(String level) {
        return switch (level) {
            case "偏瘦" -> "建议适度增加能量摄入与力量训练，关注蛋白质摄入。";
            case "正常" -> "保持当前饮食与运动习惯，继续规律记录趋势。";
            case "超重" -> "建议控制热量摄入并增加有氧+力量训练，保持每周稳定运动。";
            case "肥胖" -> "建议制定减脂计划，控制热量并提高运动频率，必要时咨询专业人士。";
            default -> "";
        };
    }
}