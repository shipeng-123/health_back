package org.example.health.util;

import org.example.health.entity.SysUser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

public class CalorieRecommendUtil {

    public static BigDecimal calcRecommended(SysUser user) {
        if (user == null) return null;

        // 缺任一项就不算推荐值
        if (user.getBirthDate() == null || user.getHeightCm() == null || user.getWeight() == null) return null;
        if (user.getActivityLevel() == null || user.getTargetType() == null) return null;

        int age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
        if (age < 0) return null;

        double w = user.getWeight().doubleValue();
        double h = user.getHeightCm().doubleValue();

        // gender: 0未知 1男 2女
        double bmr;
        Integer gender = user.getGender();
        if (gender != null && gender == 2) {
            bmr = 10 * w + 6.25 * h - 5 * age - 161;
        } else {
            bmr = 10 * w + 6.25 * h - 5 * age + 5;
        }

        // activity_level: 1低 2中 3高
        double factor;
        Integer level = user.getActivityLevel();
        if (level == 1) factor = 1.2;
        else if (level == 2) factor = 1.55;
        else if (level == 3) factor = 1.75;
        else factor = 1.2;

        double tdee = bmr * factor;

        // target_type: 1减脂 2增肌 3维持
        double rec = tdee;
        Integer target = user.getTargetType();
        if (target != null && target == 1) rec = tdee - 300;
        else if (target != null && target == 2) rec = tdee + 300;

        if (rec < 800) rec = 800;

        return BigDecimal.valueOf(rec).setScale(2, RoundingMode.HALF_UP);
    }
}