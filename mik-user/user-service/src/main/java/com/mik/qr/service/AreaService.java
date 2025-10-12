package com.mik.qr.service;

import com.mik.qr.entity.AreaEntity;
import com.mik.qr.mapper.AreaMapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AreaService extends ServiceImpl<AreaMapper, AreaEntity> {
}
