package com.mik.qr.controller;

import com.mik.core.pojo.Result;
import com.mik.qr.controller.dto.Info;
import com.mik.qr.entity.AreaEntity;
import com.mik.qr.entity.StaffEntity;
import com.mik.qr.service.AreaService;
import com.mik.qr.service.StaffService;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class InfoController {

    @Autowired
    private AreaService areaService;
    @Autowired
    private StaffService staffService;

    @GetMapping("/info/{id}")
    public Result<Info> info(@PathVariable @NotBlank String id){
        QueryCondition condition =  QueryCondition.create(new QueryColumn("uid"), "=", id);
        AreaEntity areaEntity = areaService.getMapper().selectOneByCondition(condition);
        if(areaEntity.getStaffId() == null){
            return Result.success(new Info().setName("暂无联系人").setTelephone("--"));
        }
        StaffEntity staff = staffService.getMapper().selectOneById(areaEntity.getStaffId());
        return Result.success(new Info().setName(staff.getContact()).setTelephone(staff.getTelephone()));
    }
}
