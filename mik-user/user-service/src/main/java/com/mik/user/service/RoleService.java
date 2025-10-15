package com.mik.user.service;

import com.mik.core.exception.ServiceException;
import com.mik.core.pojo.PageInput;
import com.mik.core.pojo.PageResult;
import com.mik.user.controller.cqe.RoleCreateCommand;
import com.mik.user.controller.cqe.RoleDTO;
import com.mik.user.controller.cqe.RoleQuery;
import com.mik.user.entity.Role;
import com.mik.user.entity.RolePermission;
import com.mik.user.entity.UserRole;
import com.mik.user.mapper.RoleMapper;
import com.mik.user.mapper.RolePermissionMapper;
import com.mik.user.mapper.UserRoleMapper;
import com.mik.db.entity.utils.PageUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService extends ServiceImpl<RoleMapper, Role> {

    @Resource
    RoleMapper roleMapper;
    @Resource
    RolePermissionMapper rolePermissionMapper;
    @Resource
    UserRoleMapper userRoleMapper;

    @Resource
    RolePermissionMapper mapper;

    public PageResult<RoleDTO> listRolePage(RoleQuery query, PageInput page) {
        Page<Role> paginate = Page.of(page.getPageNum(), page.getPageSize());
        QueryCondition condition =  QueryCondition.create(new QueryColumn("role_name"), "like", "%" + query.getName() + "%");
        QueryWrapper wrapper = QueryWrapper.create().select().from("role").where(condition);

        Page<Role> userListDTOS = getMapper().paginateAs(paginate, wrapper, Role.class);
        Page<RoleDTO> dtoPage = userListDTOS.map(x -> {
            RoleDTO roleDTO = new RoleDTO();
            BeanUtils.copyProperties(x, roleDTO);
            return roleDTO;
        });
        dtoPage.getRecords().forEach(x -> {
            QueryCondition condition0 =  QueryCondition.create(new QueryColumn("role_id"), "=", x.getRoleId());
            List<RolePermission> p = mapper.selectListByQuery(QueryWrapper.create().select().from("role_permission").where(condition0));
            List<Long> ids = p.stream().map(RolePermission::getPId).collect(Collectors.toList());
            x.setPermissions(ids);
        });
        return PageUtil.transform(dtoPage);
    }

    public void create(RoleCreateCommand command) {
        if(command.getRoleId() == null){
            Role role1 = getMapper().selectOneByCondition(QueryCondition.create(new QueryColumn("role_name"), "=", command.getRoleName()));
            if(role1 != null){
                throw new ServiceException("角色名称已存在");
            }
        }else if(command.getRoleId() != null){
            Role role1 = getMapper().selectOneByCondition(QueryCondition.create(new QueryColumn("role_name"), "=", command.getRoleName()));
            if(role1 != null && !role1.getRoleId().equals(command.getRoleId())){
                throw new ServiceException("角色名称已存在");
            }
        }

        Role role = new Role();
        BeanUtils.copyProperties(command, role);
        saveOrUpdate(role);

        if(command.getRoleId() != null) {
            mapper.deleteByCondition(QueryCondition.create(new QueryColumn("role_id"), "=", command.getRoleId()));
        }

        List<RolePermission> list = new ArrayList<>();
        for (String permission : command.getPIds().split(",")) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(role.getRoleId());
            rolePermission.setPId(Long.valueOf(permission));
            list.add(rolePermission);
        }
        rolePermissionMapper.insertBatch(list);

    }

    public List<RoleDTO> listUserRoles(Long userId) {
        return roleMapper.listUserRoles(userId);
    }

    public List<RoleDTO> listAllRoles() {
        return roleMapper.selectAll().stream().map(x -> {
            RoleDTO roleDTO = new RoleDTO();
            BeanUtils.copyProperties(x, roleDTO);
            return roleDTO;
        }).collect(Collectors.toList());
    }

    public void deleteRole(String ids) {
        List<UserRole> userRole = userRoleMapper.selectListByQuery(QueryWrapper.create().select("*").from("user_role").where("role_id in (" + ids + ")"));
        if(!userRole.isEmpty()){
            throw new ServiceException("角色被用户引用，无法删除");
        }
        Set<Long> set = Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toSet());
        roleMapper.deleteBatchByIds(set);
    }
}
