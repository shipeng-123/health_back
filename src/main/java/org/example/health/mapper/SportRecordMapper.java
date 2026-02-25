package org.example.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.health.entity.SportRecord;

@Mapper
public interface SportRecordMapper extends BaseMapper<SportRecord> {
}