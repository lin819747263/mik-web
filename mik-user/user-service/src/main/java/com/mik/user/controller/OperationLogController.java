package com.mik.user.controller;

import com.mik.core.pojo.PageInput;
import com.mik.core.pojo.PageResult;
import com.mik.core.pojo.Result;
import com.mik.db.entity.utils.PageUtil;
import com.mik.sys.controller.dto.OperationLogListInput;
import com.mik.sys.controller.dto.OperationLogOutput;
import com.mik.sys.entity.OperationLogEntity;
import com.mik.sys.service.OperationService;
import com.mik.user.entity.User;
import com.mik.user.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryOrderBy;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sys/operation/log")
public class OperationLogController {
    @Autowired
    OperationService operationService;

    @Autowired
    UserService userService;


    @GetMapping("listOperationLog")
    public Result<PageResult<OperationLogOutput>> listOperationLog(OperationLogListInput input, PageInput page) {
        Page<OperationLogEntity> paginate = Page.of(page.getPageNum(), page.getPageSize());
        QueryCondition condition =  QueryCondition.createEmpty();
        if (StringUtils.isNotBlank(input.getKeyword())){
            condition.and(QueryCondition.create(new QueryColumn("ip"), "like", "%" + input.getKeyword() + "%"));
        }
        if(input.getStartTime() != null&& input.getEndTime() != null){
            condition.and(QueryCondition.create(new QueryColumn("create_time"), ">=", input.getStartTime()));
            condition.and(QueryCondition.create(new QueryColumn("create_time"), "<=", input.getEndTime()));
        }
        QueryWrapper wrapper = QueryWrapper.create().select().from("operation_log").where(condition).orderBy(new QueryOrderBy(new QueryColumn("create_time"), "DESC"));

        Page<OperationLogEntity> userListDTOS = operationService.getMapper().paginateAs(paginate, wrapper, OperationLogEntity.class);
        Page<OperationLogOutput> dtoPage = userListDTOS.map(x -> {
            OperationLogOutput roleDTO = new OperationLogOutput();
            BeanUtils.copyProperties(x, roleDTO);
            User user = userService.getMapper().selectOneById(roleDTO.getUserId());
            if(user != null){
                roleDTO.setUsername(user.getUsername());
            }

            return roleDTO;
        });
        return Result.success(PageUtil.transform(dtoPage));
    }
}
