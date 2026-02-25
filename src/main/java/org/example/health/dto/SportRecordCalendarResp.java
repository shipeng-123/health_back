package org.example.health.dto;

import lombok.Data;

import java.util.List;

@Data
public class SportRecordCalendarResp {
    private Integer year;
    private Integer month;
    private List<SportRecordCalendarDayResp> days;
}