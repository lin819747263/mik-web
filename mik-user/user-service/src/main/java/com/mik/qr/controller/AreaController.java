package com.mik.qr.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.mik.core.pojo.PageInput;
import com.mik.core.pojo.PageResult;
import com.mik.core.pojo.Result;
import com.mik.file.config.StaticResourceConfigure;
import com.mik.qr.controller.dto.AreaCreateInput;
import com.mik.qr.controller.dto.AreaListInput;
import com.mik.qr.controller.dto.AreaListOutput;
import com.mik.qr.controller.dto.HisAreaListOutput;
import com.mik.qr.entity.AreaEntity;
import com.mik.qr.entity.HisAreaEntity;
import com.mik.qr.service.AreaService;
import com.mik.qr.service.HisAreaService;
import com.mik.qr.service.StaffService;
import com.mik.db.entity.utils.PageUtil;
import com.mik.sys.OperationLog;
import com.mik.user.mapper.UserMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryOrderBy;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("area")
public class AreaController {

    @Autowired
    private AreaService areaService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private HisAreaService hisAreaService;
    @Autowired
    private StaffService staffService;
    @Value("${qr.path}")
    private String qrPath;

    @Autowired
    private StaticResourceConfigure staticResourceConfigure;

    @OperationLog(operation = "新增/编辑二维码")
    @PostMapping("createArea")
    public Result createArea(@Valid AreaCreateInput input) {
        AreaEntity role = new AreaEntity();
        if(input.getAreaId() == null){
            String uid = md5();
            QrCodeUtil.generate(qrPath + uid, 300, 300, FileUtil.file(staticResourceConfigure.getPath() + "qr/" + uid + ".png"));
            String url = staticResourceConfigure.getUrl() + "qr/" + uid + ".png";
            role.setUid(uid);
            role.setQrUrl(url);
        }
        BeanUtils.copyProperties(input, role);

        areaService.saveOrUpdate(role);
        input.setAreaId(role.getAreaId());
        hisAreaService.record(input);

        return Result.success();
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

    @OperationLog(operation = "删除二维码")
    @PostMapping("delArea")
    public Result delArea(Long areaId) {
        areaService.removeById(areaId);
        return Result.success();
    }

    @PostMapping("bindStaff")
    public Result bindStaff(Long areaId, Long staffId) {
        AreaEntity areaEntity = areaService.getMapper().selectOneById(areaId);
        areaEntity.setStaffId(staffId);
        areaService.saveOrUpdate(areaEntity);
        return Result.success();
    }

    @PostMapping("getArea")
    public Result<AreaListOutput> getArea(Long areaId) {
        AreaEntity areaEntity = areaService.getMapper().selectOneById(areaId);
        AreaListOutput output = new AreaListOutput();
        BeanUtils.copyProperties(areaEntity, output);
        return Result.success(output);
    }

    @PostMapping("getAreaByUid")
    public Result<AreaListOutput> getAreaByUid(@NotBlank String uid) {
        QueryCondition condition =  QueryCondition.create(new QueryColumn("uid"), "=", uid);
        AreaEntity areaEntity = areaService.getMapper().selectOneByCondition(condition);
        if(areaEntity == null){
            return Result.success(new AreaListOutput().setContent("暂无信息"));
        }
        AreaListOutput output = new AreaListOutput();
        BeanUtils.copyProperties(areaEntity, output);
        return Result.success(output);
    }

    @PostMapping("listAreaPage")
    public Result<PageResult<AreaListOutput>> listAreaPage(AreaListInput input, PageInput page) {
        Page<AreaEntity> paginate = Page.of(page.getPageNum(), page.getPageSize());
        QueryCondition condition =  QueryCondition.createEmpty();
        if (StringUtils.isNotBlank(input.getArea())){
            condition.and(QueryCondition.create(new QueryColumn("area"), "like", "%" + input.getArea() + "%"));
        }
        if(input.getStartTime() != null&& input.getEndTime() != null){
            condition.and(QueryCondition.create(new QueryColumn("create_time"), ">=", input.getStartTime()));
            condition.and(QueryCondition.create(new QueryColumn("create_time"), "<=", input.getEndTime()));
        }
        QueryWrapper wrapper = QueryWrapper.create().select().from("area").where(condition);

        Page<AreaEntity> userListDTOS = staffService.getMapper().paginateAs(paginate, wrapper, AreaEntity.class);
        Page<AreaListOutput> dtoPage = userListDTOS.map(x -> {
            AreaListOutput roleDTO = new AreaListOutput();
            BeanUtils.copyProperties(x, roleDTO);
            return roleDTO;
        });
        return Result.success(PageUtil.transform(dtoPage));
    }

    @PostMapping("listHisAreaPage")
    public Result<List<HisAreaListOutput>> listHisAreaPage(Long areaId) {
        QueryCondition condition =  QueryCondition.create(new QueryColumn("area_id"), "=", areaId);
        QueryWrapper wrapper = QueryWrapper.create().select().from("his_area").where(condition)
                .orderBy(new QueryOrderBy(new QueryColumn("create_time"), "DESC"));
        List<HisAreaEntity> list = hisAreaService.list(wrapper);

        List<HisAreaListOutput> dtoPage = list.stream().map(x -> {
            HisAreaListOutput roleDTO = new HisAreaListOutput();
            BeanUtils.copyProperties(x, roleDTO);

            roleDTO.setUserName(userMapper.selectOneById(x.getUserId()).getUsername());
            return roleDTO;
        }).collect(Collectors.toList());
        return Result.success(dtoPage);
    }

}
