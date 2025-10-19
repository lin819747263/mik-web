package com.mik.user.service;

import com.mik.core.pojo.PageInput;
import com.mik.core.pojo.PageResult;
import com.mik.user.controller.cqe.PermissionCreateCommand;
import com.mik.user.controller.cqe.PermissionDTO;
import com.mik.user.controller.cqe.PermissionQuery;
import com.mik.user.entity.Permission;
import com.mik.user.mapper.PermissionMapper;
import com.mik.user.mapper.RolePermissionMapper;
import com.mik.db.entity.utils.PageUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PermissionService extends ServiceImpl<PermissionMapper, Permission> {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;


    public PageResult<PermissionDTO> listPermissionPage(PermissionQuery query, PageInput page) {
        Page<PermissionDTO> paginate = Page.of(page.getPageNum(), page.getPageSize());
        QueryCondition condition =  QueryCondition.create(new QueryColumn("name"), "like", "%" + query.getName() + "%");
//                .or(QueryCondition.create(new QueryColumn("code"), "=", query.getCode()));
        QueryWrapper wrapper = QueryWrapper.create().select().from("permission").where(condition);

        Page<PermissionDTO> userListDTOS = getMapper().paginateAs(paginate, wrapper, PermissionDTO.class);
        return PageUtil.transform(userListDTOS);
    }

    public void create(PermissionCreateCommand command) {
        if(command.getPId() == null){
            Permission permission = getMapper().selectOneByCondition(QueryCondition.create(new QueryColumn("code"), "=", command.getCode()));
            if(permission != null){
                throw new RuntimeException("权限编码已存在");
            }
        }else if(command.getParent() != null){
            Permission permission = getMapper().selectOneByCondition(QueryCondition.create(new QueryColumn("code"), "=", command.getCode()));
            if(permission != null && !permission.getPId().equals(command.getPId())){
                throw new RuntimeException("权限编码已存在");
            }
        }
        Permission permission = new Permission();
        BeanUtils.copyProperties(command, permission);
        saveOrUpdate(permission);
    }

    public List<PermissionDTO> listUserPermission(Long userId) {
        List<PermissionDTO> tree = new ArrayList<>();
        List<PermissionDTO> res = permissionMapper.listUserPermission(userId);

        Map<Long, PermissionDTO> collect = res.stream().collect(Collectors.toMap(PermissionDTO::getPId, Function.identity()));

        res.forEach(x -> {
            if(x.getParent() == 0) {
                tree.add(x);
            }else {
                collect.get(x.getParent()).getChildren().add(x);
            }
        });
        return tree;
    }

    public List<PermissionDTO> listAllPermission() {
        List<PermissionDTO> tree = new ArrayList<>();
        List<PermissionDTO> res = new ArrayList<>();
        getMapper().selectAll().forEach(x -> {
            PermissionDTO dto = new PermissionDTO();
            BeanUtils.copyProperties(x, dto);
            res.add(dto);
        });

        Map<Long, PermissionDTO> collect = res.stream().collect(Collectors.toMap(PermissionDTO::getPId, Function.identity()));

        res.forEach(x -> {
            if(x.getParent() == 0) {
                tree.add(x);
            }else {
                collect.get(x.getParent()).getChildren().add(x);
            }
        });

        return tree;
    }

    public void delPermission(Collection<Long> ids) {
        permissionMapper.deleteBatchByIds(ids);
        ids.forEach(x -> {
            rolePermissionMapper.deleteByCondition(QueryCondition.create(new QueryColumn("p_id"), "=",x));
        });
    }
}
