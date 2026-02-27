package org.example.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.health.entity.BodyMetricRecord;

@Mapper
public interface BodyMetricRecordMapper extends BaseMapper<BodyMetricRecord> {
}