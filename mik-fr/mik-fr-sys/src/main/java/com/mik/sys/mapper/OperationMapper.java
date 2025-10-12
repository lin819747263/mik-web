package com.mik.sys.mapper;

import com.mik.sys.entity.OperationLogEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationMapper extends BaseMapper<OperationLogEntity> {

}
