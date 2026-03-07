package org.example.health.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ApplyTemplateReq {

    @NotNull(message = "templateId不能为空")
    private Long templateId;

    @NotNull(message = "weekStartDate不能为空")
    private LocalDate weekStartDate; // 周一

    private String name; // 可选，不传就用模板名
}