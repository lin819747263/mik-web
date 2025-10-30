package com.mik.qr.service;

import com.mik.qr.controller.dto.AreaCreateInput;
import com.mik.qr.entity.AreaEntity;
import com.mik.qr.entity.HisAreaEntity;
import com.mik.qr.mapper.AreaMapper;
import com.mik.qr.mapper.HisAreaMapper;
import com.mik.security.UserContext;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class HisAreaService extends ServiceImpl<HisAreaMapper, HisAreaEntity> {


    public void record(Long areaId, String areaName, String content) {
        HisAreaEntity entity = new HisAreaEntity();
        entity.setAreaId(areaId);
        entity.setUid("");
        entity.setContent(content);
        entity.setArea(areaName);
        entity.setUserId(UserContext.getUserId());
        this.save(entity);
    }
}
