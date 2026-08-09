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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
            condition.and(QueryCondition.create(new QueryColumn("ip"), "like", "%"
                    + input.getKeyword().replace("%", "\\%").replace("_", "\\_") + "%"));
        }
        if(input.getStartTime() != null&& input.getEndTime() != null){
            condition.and(QueryCondition.create(new QueryColumn("create_time"), ">=", input.getStartTime()));
            condition.and(QueryCondition.create(new QueryColumn("create_time"), "<=", input.getEndTime()));
        }
        QueryWrapper wrapper = QueryWrapper.create().select().from("operation_log").where(condition)
                .orderBy(new QueryOrderBy(new QueryColumn("create_time"), "DESC"));

        Page<OperationLogEntity> logPage = operationService.getMapper().paginateAs(paginate, wrapper, OperationLogEntity.class);

        // 批量查询用户，避免 N+1
        Set<Long> userIds = logPage.getRecords().stream()
                .map(OperationLogEntity::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> usernameMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userService.getMapper().selectListByQuery(
                    QueryWrapper.create().select().from("user")
                            .where(new QueryColumn("user_id").in(userIds)));
            users.forEach(u -> usernameMap.put(u.getUserId(), u.getUsername()));
        }

        Map<Long, String> finalUsernameMap = usernameMap;
        Page<OperationLogOutput> dtoPage = logPage.map(x -> {
            OperationLogOutput output = new OperationLogOutput();
            BeanUtils.copyProperties(x, output);
            if (x.getUserId() != null) {
                output.setUsername(finalUsernameMap.get(x.getUserId()));
            }
            return output;
        });
        return Result.success(PageUtil.transform(dtoPage));
    }
}
