package com.mik.qr.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.mik.file.config.StaticResourceConfigure;
import com.mik.qr.controller.dto.QrCodeImportDTO;
import com.mik.qr.entity.AreaEntity;
import com.mik.qr.mapper.AreaMapper;
import com.mik.user.mapper.UserMapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AreaService extends ServiceImpl<AreaMapper, AreaEntity> {

    @Autowired
    private HisAreaService hisAreaService;
    @Value("${qr.path}")
    private String qrPath;

    @Autowired
    private StaticResourceConfigure staticResourceConfigure;


    public void batchGen(List<QrCodeImportDTO> cachedDataList) {
        cachedDataList.forEach(this::createArea);
    }

    private void createArea(QrCodeImportDTO dto) {
        AreaEntity role = new AreaEntity();
        String uid = md5();
        QrCodeUtil.generate(qrPath + uid, 300, 300, FileUtil.file(staticResourceConfigure.getPath() + "qr/" + uid + ".png"));
        String url = staticResourceConfigure.getUrl() + "qr/" + uid + ".png";
        role.setUid(uid);
        role.setQrUrl(url);

        BeanUtils.copyProperties(dto, role);

        saveOrUpdate(role);
        hisAreaService.record(role.getAreaId(), dto.getArea(), dto.getContent());
    }

    private String md5(){
        // 获取当前时间戳（毫秒）
        long timestamp = System.currentTimeMillis();
        // 生成一个随机数
        int random = new Random().nextInt();
        // 拼接字符串
        String input = timestamp + "" + random;
        return DigestUtil.md5Hex(input).substring(0, 8);
    }
}
