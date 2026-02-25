package org.example.health.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyCalorieSummaryResp {
    private LocalDate date;

    private BigDecimal intakeCalories;        // 摄入
    private BigDecimal sportCalories;         // 消耗
    private BigDecimal recommendedCalories;   // 推荐摄入

    private BigDecimal netCalories;           // 净摄入=摄入-消耗
    private BigDecimal diffVsRecommended;     // 净摄入-推荐（正=超了）

    private Boolean profileReady;
    private String profileTip;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getIntakeCalories() { return intakeCalories; }
    public void setIntakeCalories(BigDecimal intakeCalories) { this.intakeCalories = intakeCalories; }

    public BigDecimal getSportCalories() { return sportCalories; }
    public void setSportCalories(BigDecimal sportCalories) { this.sportCalories = sportCalories; }

    public BigDecimal getRecommendedCalories() { return recommendedCalories; }
    public void setRecommendedCalories(BigDecimal recommendedCalories) { this.recommendedCalories = recommendedCalories; }

    public BigDecimal getNetCalories() { return netCalories; }
    public void setNetCalories(BigDecimal netCalories) { this.netCalories = netCalories; }

    public BigDecimal getDiffVsRecommended() { return diffVsRecommended; }
    public void setDiffVsRecommended(BigDecimal diffVsRecommended) { this.diffVsRecommended = diffVsRecommended; }

    public Boolean getProfileReady() { return profileReady; }
    public void setProfileReady(Boolean profileReady) { this.profileReady = profileReady; }

    public String getProfileTip() { return profileTip; }
    public void setProfileTip(String profileTip) { this.profileTip = profileTip; }
}