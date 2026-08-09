package com.mik.user.service;

import cn.hutool.core.util.StrUtil;
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
import java.util.Map;
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

    public PageResult<RoleDTO> listRolePage(RoleQuery query, PageInput page) {
        Page<Role> paginate = Page.of(page.getPageNum(), page.getPageSize());
        QueryWrapper wrapper = QueryWrapper.create().select().from("role");
        if (StrUtil.isNotBlank(query.getName())) {
            wrapper.and(new QueryColumn("role_name").like("%" + query.getName() + "%"));
        }

        Page<Role> rolePage = getMapper().paginateAs(paginate, wrapper, Role.class);
        Page<RoleDTO> dtoPage = rolePage.map(x -> {
            RoleDTO roleDTO = new RoleDTO();
            BeanUtils.copyProperties(x, roleDTO);
            return roleDTO;
        });

        // 批量查询所有角色的权限，避免 N+1 查询
        List<Long> roleIds = dtoPage.getRecords().stream()
                .map(RoleDTO::getRoleId).collect(Collectors.toList());
        if (!roleIds.isEmpty()) {
            List<RolePermission> allPerms = rolePermissionMapper.selectListByQuery(
                    QueryWrapper.create().select().from("role_permission")
                            .where(new QueryColumn("role_id").in(roleIds)));
            Map<Long, List<Long>> permMap = allPerms.stream()
                    .collect(Collectors.groupingBy(
                            RolePermission::getRoleId,
                            Collectors.mapping(RolePermission::getPId, Collectors.toList())));
            dtoPage.getRecords().forEach(x ->
                    x.setPermissions(permMap.getOrDefault(x.getRoleId(), new ArrayList<>())));
        }

        return PageUtil.transform(dtoPage);
    }

    public void create(RoleCreateCommand command) {
        Role existing = getMapper().selectOneByCondition(
                QueryCondition.create(new QueryColumn("role_name"), "=", command.getRoleName()));
        if (existing != null) {
            if (command.getRoleId() == null || !existing.getRoleId().equals(command.getRoleId())) {
                throw new ServiceException("角色名称已存在");
            }
        }

        Role role = new Role();
        BeanUtils.copyProperties(command, role);
        saveOrUpdate(role);

        if (command.getRoleId() != null) {
            rolePermissionMapper.deleteByCondition(
                    QueryCondition.create(new QueryColumn("role_id"), "=", command.getRoleId()));
        }

        List<RolePermission> list = new ArrayList<>();
        for (String permission : command.getPIds().split(",")) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(role.getRoleId());
            rolePermission.setPId(Long.valueOf(permission.trim()));
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
        Set<Long> idSet = Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toSet());
        List<UserRole> userRole = userRoleMapper.selectListByQuery(
                QueryWrapper.create().select().from("user_role")
                        .where(new QueryColumn("role_id").in(idSet)));
        if(!userRole.isEmpty()){
            throw new ServiceException("角色被用户引用，无法删除");
        }
        roleMapper.deleteBatchByIds(idSet);
        idSet.forEach(roleId -> {
            rolePermissionMapper.deleteByCondition(QueryCondition.create(new QueryColumn("role_id"), "=", roleId));
        });
    }
}
