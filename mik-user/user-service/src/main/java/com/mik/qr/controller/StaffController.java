package com.mik.qr.controller;

import com.mik.exception.QRConstant;
import com.mik.core.exception.ServiceException;
import com.mik.core.pojo.PageInput;
import com.mik.core.pojo.PageResult;
import com.mik.core.pojo.Result;
import com.mik.qr.controller.dto.StaffCreateInput;
import com.mik.qr.controller.dto.StaffListOutput;
import com.mik.qr.entity.StaffEntity;
import com.mik.qr.service.AreaService;
import com.mik.qr.service.StaffService;
import com.mik.user.utils.PageUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("staff")
public class StaffController {

    @Autowired
    private AreaService areaService;
    @Autowired
    private StaffService staffService;
    @PostMapping("createStaff")
    public Result<Void> createStaff(@Valid StaffCreateInput input) {
        QueryCondition condition =  QueryCondition.create(new QueryColumn("contact"), "=", input.getContact()).and(
                QueryCondition.create(new QueryColumn("telephone"), "=", input.getTelephone())
        );
        StaffEntity one = staffService.getOne(condition);
        if(one != null){
            throw new ServiceException(QRConstant.USER_AUTH_EXIST);
        }
        StaffEntity role = new StaffEntity();
        BeanUtils.copyProperties(input, role);
        staffService.saveOrUpdate(role);
        return Result.success();
    }

    @PostMapping("delStaff")
    public Result<Void> delStaff(Long StaffId) {
        staffService.removeById(StaffId);
        return Result.success();
    }

    @PostMapping("getStaff")
    public Result<StaffListOutput> getStaff(Long StaffId) {
        StaffEntity staffEntity = staffService.getMapper().selectOneById(StaffId);
        StaffListOutput output = new StaffListOutput();
        BeanUtils.copyProperties(staffEntity, output);
        return Result.success(output);
    }

    @PostMapping("listStaffPage")
    public Result<PageResult<StaffListOutput>> listStaffPage(String keyword, PageInput page) {
        Page<StaffEntity> paginate = Page.of(page.getPageNum(), page.getPageSize());
        QueryCondition condition =  QueryCondition.create(new QueryColumn("contact"), "like", "%" + keyword + "%");
        QueryWrapper wrapper = QueryWrapper.create().select().from("staff").where(condition);

        Page<StaffEntity> userListDTOS = staffService.getMapper().paginateAs(paginate, wrapper, StaffEntity.class);
        Page<StaffListOutput> dtoPage = userListDTOS.map(x -> {
            StaffListOutput roleDTO = new StaffListOutput();
            BeanUtils.copyProperties(x, roleDTO);
            return roleDTO;
        });
        return Result.success(PageUtil.transform(dtoPage));
    }
}
