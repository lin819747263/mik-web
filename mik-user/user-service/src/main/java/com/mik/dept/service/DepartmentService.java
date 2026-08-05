package com.mik.dept.service;

import com.mik.core.exception.ServiceException;
import com.mik.dept.dto.DeptCreateCommand;
import com.mik.dept.dto.DeptDTO;
import com.mik.dept.dto.DeptTreeDTO;
import com.mik.dept.entity.Department;
import com.mik.dept.entity.DeptPermission;
import com.mik.dept.mapper.DepartmentMapper;
import com.mik.dept.mapper.DeptPermissionMapper;
import com.mik.user.entity.User;
import com.mik.user.mapper.UserMapper;
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
public class DepartmentService extends ServiceImpl<DepartmentMapper, Department> {

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private DeptPermissionMapper deptPermissionMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 创建/编辑部门
     */
    public Long create(DeptCreateCommand command) {
        // 校验部门名称唯一（同级下）
        checkDeptName(command);

        // 校验层级不超过3级
        checkLevel(command);

        // 校验只有一个顶级部门
        checkTopDept(command);

        Department dept = new Department();
        BeanUtils.copyProperties(command, dept);

        // 计算层级
        if (command.getParentId() == null || command.getParentId() == 0) {
            dept.setParentId(0L);
            dept.setLevel(1);
        } else {
            Department parent = departmentMapper.selectOneById(command.getParentId());
            if (parent == null) {
                throw new ServiceException("父部门不存在");
            }
            dept.setLevel(parent.getLevel() + 1);
        }

        if (dept.getEnable() == null) {
            dept.setEnable(1);
        }

        // 显式区分新增和编辑，避免 saveOrUpdate 对 null ID 的行为不确定
        if (command.getDeptId() == null) {
            departmentMapper.insert(dept);
        } else {
            departmentMapper.update(dept);
        }
        return dept.getDeptId();
    }

    /**
     * 删除部门
     */
    public void delete(Long deptId) {
        // 校验是否有子部门
        QueryCondition childCondition = QueryCondition.create(new QueryColumn("parent_id"), "=", deptId);
        List<Department> children = departmentMapper.selectListByQuery(
                QueryWrapper.create().select().from("department").where(childCondition));
        if (!children.isEmpty()) {
            throw new ServiceException("该部门下有子部门，无法删除");
        }

        // 校验是否有用户关联
        QueryCondition userCondition = QueryCondition.create(new QueryColumn("dept_id"), "=", deptId);
        List<User> users = userMapper.selectListByQuery(
                QueryWrapper.create().select().from("user").where(userCondition));
        if (!users.isEmpty()) {
            throw new ServiceException("该部门下有用户，无法删除");
        }

        // 删除部门
        departmentMapper.deleteById(deptId);

        // 删除部门权限关联
        QueryCondition permCondition = QueryCondition.create(new QueryColumn("dept_id"), "=", deptId);
        deptPermissionMapper.deleteByCondition(permCondition);
    }

    /**
     * 获取部门树
     */
    public List<DeptTreeDTO> getTree() {
        List<Department> allDepts = departmentMapper.selectAll();
        List<DeptTreeDTO> dtos = allDepts.stream().map(dept -> {
            DeptTreeDTO dto = new DeptTreeDTO();
            BeanUtils.copyProperties(dept, dto);
            return dto;
        }).collect(Collectors.toList());

        return buildTree(dtos, 0L);
    }

    /**
     * 获取部门详情
     */
    public DeptDTO getDetail(Long deptId) {
        Department dept = departmentMapper.selectOneById(deptId);
        if (dept == null) {
            throw new ServiceException("部门不存在");
        }

        DeptDTO dto = new DeptDTO();
        BeanUtils.copyProperties(dept, dto);

        // 查询部门权限
        QueryCondition condition = QueryCondition.create(new QueryColumn("dept_id"), "=", deptId);
        List<DeptPermission> permissions = deptPermissionMapper.selectListByQuery(
                QueryWrapper.create().select().from("dept_permission").where(condition));
        List<Long> permissionIds = permissions.stream()
                .map(DeptPermission::getPId)
                .collect(Collectors.toList());
        dto.setPermissionIds(permissionIds);

        return dto;
    }

    /**
     * 分配部门权限
     */
    public void assignPermission(Long deptId, String pIds) {
        // 校验部门是否存在
        Department dept = departmentMapper.selectOneById(deptId);
        if (dept == null) {
            throw new ServiceException("部门不存在");
        }

        // 删除原有部门权限
        QueryCondition condition = QueryCondition.create(new QueryColumn("dept_id"), "=", deptId);
        deptPermissionMapper.deleteByCondition(condition);

        // 批量插入新权限
        if (pIds != null && !pIds.trim().isEmpty()) {
            List<DeptPermission> list = new ArrayList<>();
            Arrays.stream(pIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(pId -> {
                        DeptPermission dp = new DeptPermission();
                        dp.setDeptId(deptId);
                        dp.setPId(Long.valueOf(pId));
                        list.add(dp);
                    });
            if (!list.isEmpty()) {
                deptPermissionMapper.insertBatch(list);
            }
        }
    }

    /**
     * 获取部门权限ID列表
     */
    public List<Long> getDeptPermissionIds(Long deptId) {
        QueryCondition condition = QueryCondition.create(new QueryColumn("dept_id"), "=", deptId);
        List<DeptPermission> permissions = deptPermissionMapper.selectListByQuery(
                QueryWrapper.create().select().from("dept_permission").where(condition));
        return permissions.stream()
                .map(DeptPermission::getPId)
                .collect(Collectors.toList());
    }

    // ========== 私有方法 ==========

    private void checkDeptName(DeptCreateCommand command) {
        Long parentId = command.getParentId() != null ? command.getParentId() : 0L;

        // 新增时：查找同名同级部门
        // 编辑时：查找同名同级部门，排除自身
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from("department")
                .where(new QueryColumn("dept_name").eq(command.getDeptName()))
                .and(new QueryColumn("parent_id").eq(parentId));

        if (command.getDeptId() != null && command.getDeptId() > 0) {
            // 编辑：排除自身，包含其他所有记录（包括 null ID 的脏数据）
            wrapper.and(new QueryColumn("dept_id").ne(command.getDeptId())
                    .or(new QueryColumn("dept_id").isNull()));
        } else {
            // 新增：只查找已有记录（dept_id 不为 null 的）
            wrapper.and(new QueryColumn("dept_id").isNotNull());
        }

        Department exist = departmentMapper.selectOneByQuery(wrapper);
        if (exist != null) {
            throw new ServiceException("同级下已存在相同名称的部门");
        }
    }

    private void checkLevel(DeptCreateCommand command) {
        if (command.getParentId() != null && command.getParentId() > 0) {
            Department parent = departmentMapper.selectOneById(command.getParentId());
            if (parent != null && parent.getLevel() >= 3) {
                throw new ServiceException("最多支持三级部门");
            }
        }
    }

    private void checkTopDept(DeptCreateCommand command) {
        // 如果创建/编辑为顶级部门，检查是否已存在其他顶级部门
        if (command.getParentId() == null || command.getParentId() == 0) {
            QueryWrapper wrapper = QueryWrapper.create()
                    .select()
                    .from("department")
                    .where(new QueryColumn("parent_id").eq(0));

            if (command.getDeptId() != null && command.getDeptId() > 0) {
                // 编辑：排除自身，但保留 IS NULL 的记录
                wrapper.and(new QueryColumn("dept_id").ne(command.getDeptId())
                        .or(new QueryColumn("dept_id").isNull()));
            } else {
                // 新增：只查找已有记录
                wrapper.and(new QueryColumn("dept_id").isNotNull());
            }

            Department exist = departmentMapper.selectOneByQuery(wrapper);
            if (exist != null) {
                throw new ServiceException("已存在顶级部门，只能有一个");
            }
        }
    }

    private List<DeptTreeDTO> buildTree(List<DeptTreeDTO> allDepts, Long parentId) {
        List<DeptTreeDTO> tree = new ArrayList<>();
        Map<Long, List<DeptTreeDTO>> parentMap = allDepts.stream()
                .collect(Collectors.groupingBy(DeptTreeDTO::getParentId));

        List<DeptTreeDTO> children = parentMap.getOrDefault(parentId, new ArrayList<>());
        for (DeptTreeDTO child : children) {
            child.setChildren(buildTree(allDepts, child.getDeptId()));
            tree.add(child);
        }

        return tree;
    }
}
