package com.mik.qr;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.mik.qr.controller.dto.QrCodeImportDTO;
import com.mik.qr.service.AreaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
public class QrCodeImportListener implements ReadListener<QrCodeImportDTO> {

    // 缓存的数据，可用于批量处理
    private static final int BATCH_COUNT = 100;
    private List<QrCodeImportDTO> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    // 用于接收处理后的数据（可以是 Service 或 Repository）
    private final AreaService areaService;

    public QrCodeImportListener(AreaService qrCodeService) {
        this.areaService = qrCodeService;
    }

    // 每读取一行数据就会调用一次
    @Override
    public void invoke(QrCodeImportDTO data, AnalysisContext context) {
        log.info("解析到一条数据: {}", data);
        cachedDataList.add(data);

        // 达到批处理数量时，保存到数据库
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    // 所有数据读取完毕后调用
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info("所有数据解析完成！");
    }

    // 批量保存数据
    private void saveData() {
        if (CollectionUtils.isEmpty(cachedDataList)) {
            return;
        }
        areaService.batchGen(cachedDataList);
        log.info("成功保存 {} 条数据", cachedDataList.size());
    }
}
