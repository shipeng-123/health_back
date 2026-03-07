package org.example.health.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlanCheckinReq {

    @NotNull(message = "planItemId不能为空")
    private Long planItemId;

    // 可选：true=完成；false=取消完成；不传默认完成
    private Boolean done;
}