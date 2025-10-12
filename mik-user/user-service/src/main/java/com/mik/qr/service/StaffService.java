package com.mik.qr.service;

import com.mik.qr.entity.StaffEntity;
import com.mik.qr.mapper.StaffMapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class StaffService extends ServiceImpl<StaffMapper, StaffEntity> {
}
