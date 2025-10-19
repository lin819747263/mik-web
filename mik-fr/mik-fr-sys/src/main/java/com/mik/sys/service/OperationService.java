package com.mik.sys.service;

import com.mik.sys.entity.OperationLogEntity;
import com.mik.sys.mapper.OperationMapper;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class OperationService extends ServiceImpl<OperationMapper, OperationLogEntity> {


    @Scheduled(cron = "0 0 0 * * ?")
    public void clearLog(){
        // 获取当前日期
        LocalDate now = LocalDate.now();

        // 减去一个月
        LocalDate lastMonth = now.minusMonths(1);

        // 定义格式器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 转为字符串
        String result = lastMonth.format(formatter);
        getMapper().deleteByCondition(QueryCondition.create(new QueryColumn("create_time"), "<=", result));
    }

}
