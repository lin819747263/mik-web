package com.mik.sys.service;

import com.mik.sys.entity.OperationLogEntity;
import com.mik.sys.mapper.OperationMapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

@Service
public class OperationService extends ServiceImpl<OperationMapper, OperationLogEntity> {

}
