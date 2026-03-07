package org.example.health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import org.example.health.common.ApiResult;
import org.example.health.dto.*;
import org.example.health.entity.*;
import org.example.health.mapper.*;
import org.example.health.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/plan")
public class SportPlanController {

    private final SportPlanTemplateMapper templateMapper;
    private final SportPlanTemplateItemMapper templateItemMapper;
    private final UserSportPlanMapper userPlanMapper;
    private final UserSportPlanItemMapper userPlanItemMapper;
    private final SportRecordMapper sportRecordMapper;
    private final SysUserMapper sysUserMapper;

    public SportPlanController(SportPlanTemplateMapper templateMapper,
                               SportPlanTemplateItemMapper templateItemMapper,
                               UserSportPlanMapper userPlanMapper,
                               UserSportPlanItemMapper userPlanItemMapper,
                               SportRecordMapper sportRecordMapper,
                               SysUserMapper sysUserMapper) {
        this.templateMapper = templateMapper;
        this.templateItemMapper = templateItemMapper;
        this.userPlanMapper = userPlanMapper;
        this.userPlanItemMapper = userPlanItemMapper;
        this.sportRecordMapper = sportRecordMapper;
        this.sysUserMapper = sysUserMapper;
    }

    // ========== 1) 模板列表 ==========
    @GetMapping("/template/list")
    public ApiResult<List<PlanTemplateResp>> listTemplates() {
        List<SportPlanTemplate> list = templateMapper.selectList(
                new LambdaQueryWrapper<SportPlanTemplate>()
                        .eq(SportPlanTemplate::getStatus, 1)
                        .orderByDesc(SportPlanTemplate::getId)
        );

        List<PlanTemplateResp> resp = list.stream().map(t -> {
            PlanTemplateResp r = new PlanTemplateResp();
            r.setId(t.getId());
            r.setName(t.getName());
            r.setDescription(t.getDescription());
            return r;
        }).toList();

        return ApiResult.success(resp);
    }

    // ========== 2) 模板详情 ==========
    @GetMapping("/template/{id}")
    public ApiResult<PlanTemplateDetailResp> templateDetail(@PathVariable("id") Long id) {
        SportPlanTemplate t = templateMapper.selectById(id);
        if (t == null || t.getStatus() == null || t.getStatus() == 0) {
            return ApiResult.fail("模板不存在");
        }

        List<SportPlanTemplateItem> items = templateItemMapper.selectList(
                new LambdaQueryWrapper<SportPlanTemplateItem>()
                        .eq(SportPlanTemplateItem::getTemplateId, id)
                        .orderByAsc(SportPlanTemplateItem::getDayOfWeek)
                        .orderByAsc(SportPlanTemplateItem::getSortNo)
                        .orderByAsc(SportPlanTemplateItem::getId)
        );

        PlanTemplateDetailResp resp = new PlanTemplateDetailResp();
        resp.setId(t.getId());
        resp.setName(t.getName());
        resp.setDescription(t.getDescription());
        resp.setItems(items.stream().map(it -> {
            PlanTemplateItemResp r = new PlanTemplateItemResp();
            r.setDayOfWeek(it.getDayOfWeek());
            r.setSportType(it.getSportType());
            r.setTargetDurationMin(it.getTargetDurationMin());
            r.setTargetDistanceKm(it.getTargetDistanceKm());
            r.setRemindTime(it.getRemindTime());
            r.setRemark(it.getRemark());
            return r;
        }).toList());

        return ApiResult.success(resp);
    }

    // ========== 3) 应用模板：生成某周计划（覆盖同周） ==========
    @PostMapping("/week/apply-template")
    public ApiResult<Map<String, Object>> applyTemplate(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                        @Valid @RequestBody ApplyTemplateReq req) {
        Long userId = JwtUtil.getUserIdFromHeader(authorization);
        if (userId == null) return ApiResult.fail("未登录");

        LocalDate weekStart = ensureMonday(req.getWeekStartDate());

        SportPlanTemplate t = templateMapper.selectById(req.getTemplateId());
        if (t == null || t.getStatus() == null || t.getStatus() == 0) return ApiResult.fail("模板不存在");

        UserSportPlan old = userPlanMapper.selectOne(
                new LambdaQueryWrapper<UserSportPlan>()
                        .eq(UserSportPlan::getUserId, userId)
                        .eq(UserSportPlan::getWeekStartDate, weekStart)
                        .last("limit 1")
        );
        if (old != null) {
            userPlanItemMapper.delete(new LambdaQueryWrapper<UserSportPlanItem>()
                    .eq(UserSportPlanItem::getPlanId, old.getId()));
            userPlanMapper.deleteById(old.getId());
        }

        String planName = (req.getName() != null && !req.getName().isBlank()) ? req.getName() : t.getName();

        UserSportPlan plan = new UserSportPlan();
        plan.setUserId(userId);
        plan.setWeekStartDate(weekStart);
        plan.setName(planName);
        plan.setSourceType(1);
        plan.setTemplateId(t.getId());
        userPlanMapper.insert(plan);

        List<SportPlanTemplateItem> templateItems = templateItemMapper.selectList(
                new LambdaQueryWrapper<SportPlanTemplateItem>()
                        .eq(SportPlanTemplateItem::getTemplateId, t.getId())
                        .orderByAsc(SportPlanTemplateItem::getDayOfWeek)
                        .orderByAsc(SportPlanTemplateItem::getSortNo)
        );

        for (SportPlanTemplateItem it : templateItems) {
            UserSportPlanItem pi = new UserSportPlanItem();
            pi.setPlanId(plan.getId());
            pi.setUserId(userId);
            pi.setDayOfWeek(it.getDayOfWeek());
            pi.setPlanDate(weekStart.plusDays(it.getDayOfWeek() - 1L));
            pi.setSportType(it.getSportType());
            pi.setTargetDurationMin(it.getTargetDurationMin());
            pi.setTargetDistanceKm(it.getTargetDistanceKm());
            pi.setRemindTime(it.getRemindTime());
            pi.setRemark(it.getRemark());
            pi.setDone(0);
            userPlanItemMapper.insert(pi);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("planId", plan.getId());
        data.put("weekStartDate", weekStart);
        data.put("name", plan.getName());
        return ApiResult.success(data);
    }

    @GetMapping("/week")
    public ApiResult<WeekPlanResp> week(@RequestHeader(value = "Authorization", required = false) String authorization,
                                        @RequestParam("weekStartDate") LocalDate weekStartDate) {
        Long userId = JwtUtil.getUserIdFromHeader(authorization);
        if (userId == null) return ApiResult.fail("未登录");

        LocalDate weekStart = ensureMonday(weekStartDate);

        UserSportPlan plan = userPlanMapper.selectOne(
                new LambdaQueryWrapper<UserSportPlan>()
                        .eq(UserSportPlan::getUserId, userId)
                        .eq(UserSportPlan::getWeekStartDate, weekStart)
                        .last("limit 1")
        );

        WeekPlanResp resp = new WeekPlanResp();
        resp.setWeekStartDate(weekStart);

        if (plan == null) {
            resp.setPlanId(null);
            resp.setPlanName(null);
            resp.setItems(Collections.emptyList());
            resp.setTotalCount(0);
            resp.setDoneCount(0);
            resp.setCompletionRate(0.0);
            return ApiResult.success(resp);
        }

        List<UserSportPlanItem> items = userPlanItemMapper.selectList(
                new LambdaQueryWrapper<UserSportPlanItem>()
                        .eq(UserSportPlanItem::getUserId, userId)
                        .eq(UserSportPlanItem::getPlanId, plan.getId())
                        .between(UserSportPlanItem::getPlanDate, weekStart, weekStart.plusDays(6))
                        .orderByAsc(UserSportPlanItem::getPlanDate)
                        .orderByAsc(UserSportPlanItem::getId)
        );

        int total = items.size();
        int done = (int) items.stream().filter(x -> x.getDone() != null && x.getDone() == 1).count();
        double rate = total == 0 ? 0.0 : (done * 1.0 / total);

        resp.setPlanId(plan.getId());
        resp.setPlanName(plan.getName());
        resp.setTotalCount(total);
        resp.setDoneCount(done);
        resp.setCompletionRate(rate);

        resp.setItems(items.stream().map(x -> {
            WeekPlanItemResp r = new WeekPlanItemResp();
            r.setId(x.getId());
            r.setPlanDate(x.getPlanDate());
            r.setDayOfWeek(x.getDayOfWeek());
            r.setSportType(x.getSportType());
            r.setTargetDurationMin(x.getTargetDurationMin());
            r.setTargetDistanceKm(x.getTargetDistanceKm());
            r.setRemindTime(x.getRemindTime());
            r.setRemark(x.getRemark());
            r.setDone(x.getDone());
            r.setDoneTime(x.getDoneTime());
            return r;
        }).toList());

        return ApiResult.success(resp);
    }

    // ========== 4) 保存自定义周计划（覆盖同周） ==========
    @PostMapping("/week/save")
    public ApiResult<Map<String, Object>> saveWeekPlan(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                       @Valid @RequestBody SaveWeekPlanReq req) {
        Long userId = JwtUtil.getUserIdFromHeader(authorization);
        if (userId == null) return ApiResult.fail("未登录");

        LocalDate weekStart = ensureMonday(req.getWeekStartDate());
        String planName = (req.getName() == null || req.getName().isBlank())
                ? "我的周计划"
                : req.getName();

        UserSportPlan old = userPlanMapper.selectOne(
                new LambdaQueryWrapper<UserSportPlan>()
                        .eq(UserSportPlan::getUserId, userId)
                        .eq(UserSportPlan::getWeekStartDate, weekStart)
                        .last("limit 1")
        );
        if (old != null) {
            userPlanItemMapper.delete(new LambdaQueryWrapper<UserSportPlanItem>()
                    .eq(UserSportPlanItem::getPlanId, old.getId()));
            userPlanMapper.deleteById(old.getId());
        }

        UserSportPlan plan = new UserSportPlan();
        plan.setUserId(userId);
        plan.setWeekStartDate(weekStart);
        plan.setName(planName);
        plan.setSourceType(2);
        plan.setTemplateId(null);
        userPlanMapper.insert(plan);

        if (req.getItems() != null) {
            for (WeekPlanItemReq it : req.getItems()) {
                if (it.getDayOfWeek() == null || it.getDayOfWeek() < 1 || it.getDayOfWeek() > 7) continue;

                UserSportPlanItem pi = new UserSportPlanItem();
                pi.setPlanId(plan.getId());
                pi.setUserId(userId);
                pi.setDayOfWeek(it.getDayOfWeek());
                pi.setPlanDate(weekStart.plusDays(it.getDayOfWeek() - 1L));
                pi.setSportType(it.getSportType());
                pi.setTargetDurationMin(it.getTargetDurationMin());
                pi.setTargetDistanceKm(it.getTargetDistanceKm());
                pi.setRemindTime(it.getRemindTime());
                pi.setRemark(it.getRemark());
                pi.setDone(0);
                userPlanItemMapper.insert(pi);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("planId", plan.getId());
        data.put("weekStartDate", weekStart);
        data.put("name", plan.getName());
        return ApiResult.success(data);
    }

    // ========== 5) 今日计划展示 ==========
    @GetMapping("/today")
    public ApiResult<TodayPlanResp> today(@RequestHeader(value = "Authorization", required = false) String authorization,
                                          @RequestParam("date") LocalDate date) {
        Long userId = JwtUtil.getUserIdFromHeader(authorization);
        if (userId == null) return ApiResult.fail("未登录");

        LocalDate weekStart = toWeekStartMonday(date);

        UserSportPlan plan = userPlanMapper.selectOne(
                new LambdaQueryWrapper<UserSportPlan>()
                        .eq(UserSportPlan::getUserId, userId)
                        .eq(UserSportPlan::getWeekStartDate, weekStart)
                        .last("limit 1")
        );

        TodayPlanResp resp = new TodayPlanResp();
        resp.setDate(date);
        resp.setWeekStartDate(weekStart);

        if (plan == null) {
            resp.setPlanId(null);
            resp.setPlanName(null);
            resp.setItems(Collections.emptyList());
            resp.setTotalCount(0);
            resp.setDoneCount(0);
            resp.setCompletionRate(0.0);
            return ApiResult.success(resp);
        }

        List<UserSportPlanItem> items = userPlanItemMapper.selectList(
                new LambdaQueryWrapper<UserSportPlanItem>()
                        .eq(UserSportPlanItem::getUserId, userId)
                        .eq(UserSportPlanItem::getPlanId, plan.getId())
                        .eq(UserSportPlanItem::getPlanDate, date)
                        .orderByAsc(UserSportPlanItem::getId)
        );

        int total = items.size();
        int done = (int) items.stream().filter(x -> x.getDone() != null && x.getDone() == 1).count();
        double rate = total == 0 ? 0.0 : (done * 1.0 / total);

        resp.setPlanId(plan.getId());
        resp.setPlanName(plan.getName());
        resp.setTotalCount(total);
        resp.setDoneCount(done);
        resp.setCompletionRate(rate);

        resp.setItems(items.stream().map(x -> {
            TodayPlanItemResp r = new TodayPlanItemResp();
            r.setId(x.getId());
            r.setDayOfWeek(x.getDayOfWeek());
            r.setSportType(x.getSportType());
            r.setTargetDurationMin(x.getTargetDurationMin());
            r.setTargetDistanceKm(x.getTargetDistanceKm());
            r.setRemindTime(x.getRemindTime());
            r.setRemark(x.getRemark());
            r.setDone(x.getDone());
            r.setDoneTime(x.getDoneTime());
            return r;
        }).toList());

        return ApiResult.success(resp);
    }

    // ========== 6) 打卡/取消打卡 ==========
    @PostMapping("/checkin")
    public ApiResult<String> checkin(@RequestHeader(value = "Authorization", required = false) String authorization,
                                     @Valid @RequestBody PlanCheckinReq req) {
        Long userId = JwtUtil.getUserIdFromHeader(authorization);
        if (userId == null) return ApiResult.fail("未登录");

        UserSportPlanItem item = userPlanItemMapper.selectById(req.getPlanItemId());
        if (item == null || !Objects.equals(item.getUserId(), userId)) {
            return ApiResult.fail("计划项不存在");
        }

        boolean targetDone = req.getDone() == null || req.getDone();
        boolean oldDone = item.getDone() != null && item.getDone() == 1;

        item.setDone(targetDone ? 1 : 0);
        item.setDoneTime(targetDone ? LocalDateTime.now() : null);
        userPlanItemMapper.updateById(item);

        if (!oldDone && targetDone) {
            insertSportRecordFromPlan(item);
        } else if (oldDone && !targetDone) {
            deleteAutoSportRecord(item);
        }

        return ApiResult.success(targetDone ? "打卡成功" : "已取消打卡");
    }

    // ========== 7) 周完成率统计 ==========
    @GetMapping("/stat/week")
    public ApiResult<PlanStatResp> statWeek(@RequestHeader(value = "Authorization", required = false) String authorization,
                                            @RequestParam("weekStartDate") LocalDate weekStartDate) {
        Long userId = JwtUtil.getUserIdFromHeader(authorization);
        if (userId == null) return ApiResult.fail("未登录");

        LocalDate start = ensureMonday(weekStartDate);
        LocalDate end = start.plusDays(6);

        return ApiResult.success(calcStat(userId, start, end));
    }

    // ========== 8) 月完成率统计 ==========
    @GetMapping("/stat/month")
    public ApiResult<PlanStatResp> statMonth(@RequestHeader(value = "Authorization", required = false) String authorization,
                                             @RequestParam("year") Integer year,
                                             @RequestParam("month") Integer month) {
        Long userId = JwtUtil.getUserIdFromHeader(authorization);
        if (userId == null) return ApiResult.fail("未登录");

        if (year == null || month == null || month < 1 || month > 12) return ApiResult.fail("参数错误");

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        return ApiResult.success(calcStat(userId, start, end));
    }

    // ======= 内部统计方法 =======
    private PlanStatResp calcStat(Long userId, LocalDate start, LocalDate end) {
        List<UserSportPlanItem> items = userPlanItemMapper.selectList(
                new LambdaQueryWrapper<UserSportPlanItem>()
                        .eq(UserSportPlanItem::getUserId, userId)
                        .between(UserSportPlanItem::getPlanDate, start, end)
        );

        int total = items.size();
        int done = (int) items.stream().filter(x -> x.getDone() != null && x.getDone() == 1).count();
        double rate = total == 0 ? 0.0 : (done * 1.0 / total);

        PlanStatResp resp = new PlanStatResp();
        resp.setStartDate(start);
        resp.setEndDate(end);
        resp.setTotalItems(total);
        resp.setDoneItems(done);
        resp.setCompletionRate(rate);
        return resp;
    }

    // ======= 打卡后自动写运动记录 =======
    private void insertSportRecordFromPlan(UserSportPlanItem item) {
        String autoRemark = buildAutoRemark(item.getId());

        SportRecord existed = sportRecordMapper.selectOne(
                new LambdaQueryWrapper<SportRecord>()
                        .eq(SportRecord::getUserId, item.getUserId())
                        .eq(SportRecord::getRecordDate, item.getPlanDate())
                        .eq(SportRecord::getRemark, autoRemark)
                        .last("limit 1")
        );
        if (existed != null) {
            return;
        }

        BigDecimal met = getMetValue(item.getSportType());
        BigDecimal weight = getUserWeight(item.getUserId());
        BigDecimal calories = calcCalories(met, weight, item.getTargetDurationMin());

        SportRecord record = new SportRecord();
        record.setUserId(item.getUserId());
        record.setSportType(item.getSportType());
        record.setDurationMin(item.getTargetDurationMin());
        record.setDistanceKm(item.getTargetDistanceKm());
        record.setMetValue(met);
        record.setCalories(calories);
        record.setRecordDate(item.getPlanDate());
        record.setRemark(autoRemark);

        sportRecordMapper.insert(record);
    }

    private void deleteAutoSportRecord(UserSportPlanItem item) {
        String autoRemark = buildAutoRemark(item.getId());
        sportRecordMapper.delete(
                new LambdaQueryWrapper<SportRecord>()
                        .eq(SportRecord::getUserId, item.getUserId())
                        .eq(SportRecord::getRecordDate, item.getPlanDate())
                        .eq(SportRecord::getRemark, autoRemark)
        );
    }

    private String buildAutoRemark(Long planItemId) {
        return "计划打卡自动生成#" + planItemId;
    }

    private BigDecimal getUserWeight(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getWeight() == null) {
            return new BigDecimal("60");
        }
        return user.getWeight();
    }

    private BigDecimal calcCalories(BigDecimal met, BigDecimal weightKg, Integer durationMin) {
        if (met == null || weightKg == null || durationMin == null || durationMin <= 0) {
            return BigDecimal.ZERO;
        }
        // 热量 ≈ MET × 体重(kg) × 时长(h)
        BigDecimal hours = BigDecimal.valueOf(durationMin)
                .divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);

        return met.multiply(weightKg)
                .multiply(hours)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getMetValue(String sportType) {
        if (sportType == null) return BigDecimal.ZERO;
        return switch (sportType) {
            case "RUNNING" -> new BigDecimal("9.8");
            case "CYCLING" -> new BigDecimal("7.5");
            case "SWIMMING" -> new BigDecimal("8.0");
            case "STRENGTH_TRAINING" -> new BigDecimal("6.0");
            case "WALKING" -> new BigDecimal("3.8");
            case "JUMP_ROPE" -> new BigDecimal("11.0");
            case "BADMINTON" -> new BigDecimal("5.5");
            case "BASKETBALL" -> new BigDecimal("6.5");
            case "FOOTBALL" -> new BigDecimal("7.0");
            case "YOGA" -> new BigDecimal("3.0");
            case "AEROBICS" -> new BigDecimal("6.8");
            case "HIKING" -> new BigDecimal("6.0");
            default -> BigDecimal.ZERO;
        };
    }

    // ======= 周一对齐工具 =======
    private LocalDate ensureMonday(LocalDate date) {
        return toWeekStartMonday(date);
    }

    private LocalDate toWeekStartMonday(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        int offset = dow.getValue() - DayOfWeek.MONDAY.getValue();
        return date.minusDays(offset);
    }
}