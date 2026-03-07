package org.example.health.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlanTemplateDetailResp {
    private Long id;
    private String name;
    private String description;
    private List<PlanTemplateItemResp> items;
}