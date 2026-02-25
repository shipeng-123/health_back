package org.example.health.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SportMetUtil {

    private static final Map<String, BigDecimal> MET_MAP = new HashMap<>();

    static {
        MET_MAP.put("RUNNING", new BigDecimal("9.8"));             // 跑步
        MET_MAP.put("CYCLING", new BigDecimal("7.5"));             // 骑行
        MET_MAP.put("SWIMMING", new BigDecimal("8.0"));            // 游泳
        MET_MAP.put("STRENGTH_TRAINING", new BigDecimal("6.0"));   // 力量训练
        MET_MAP.put("WALKING", new BigDecimal("3.8"));             // 步行
        MET_MAP.put("JUMP_ROPE", new BigDecimal("11.0"));          // 跳绳
        MET_MAP.put("BADMINTON", new BigDecimal("5.5"));           // 羽毛球
        MET_MAP.put("BASKETBALL", new BigDecimal("6.5"));          // 篮球
        MET_MAP.put("FOOTBALL", new BigDecimal("7.0"));            // 足球
        MET_MAP.put("YOGA", new BigDecimal("3.0"));                // 瑜伽
        MET_MAP.put("AEROBICS", new BigDecimal("6.8"));            // 有氧操
        MET_MAP.put("HIKING", new BigDecimal("6.0"));              // 徒步

    }

    public static BigDecimal getMet(String sportType) {
        return MET_MAP.get(sportType);
    }

    public static boolean contains(String sportType) {
        return MET_MAP.containsKey(sportType);
    }
}