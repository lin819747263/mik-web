package com.mik.dept.controller;

import com.mik.core.exception.ServiceException;
import com.mik.core.pojo.Result;
import com.mik.dept.dto.DeptCreateCommand;
import com.mik.dept.dto.DeptDTO;
import com.mik.dept.dto.DeptTreeDTO;
import com.mik.dept.service.DepartmentService;
import com.mik.exception.SecurityConstant;
import com.mik.security.UserContext;
import com.mik.sys.OperationLog;
import com.mik.user.controller.cqe.RoleDTO;
import com.mik.user.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dept")
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    @Resource
    private RoleService roleService;

    // ========== 部门管理 ==========

    @OperationLog(operation = "创建/编辑部门")
    @PostMapping("/create")
    public Result<Map<String, Long>> create(@RequestBody DeptCreateCommand command) {
        Long deptId = departmentService.create(command);
        Map<String, Long> result = new HashMap<>();
        result.put("deptId", deptId);
        return Result.success(result);
    }

    @OperationLog(operation = "删除部门")
    @PostMapping("/delete")
    public Result delete(@RequestBody Map<String, Long> body) {
        departmentService.delete(body.get("deptId"));
        return Result.DELETE_SUCCESS;
    }

    @GetMapping("/tree")
    public Result<List<DeptTreeDTO>> tree() {
        return Result.success(departmentService.getTree());
    }

    @GetMapping("/detail")
    public Result<DeptDTO> detail(Long deptId) {
        return Result.success(departmentService.getDetail(deptId));
    }

    // ========== 部门权限管理 ==========

    @OperationLog(operation = "分配部门权限")
    @PostMapping("/assignPermission")
    public Result assignPermission(@RequestBody Map<String, Object> body) {
        checkAdmin();
        Object deptIdObj = body.get("deptId");
        if (deptIdObj == null) {
            return Result.error("部门ID不能为空");
        }
        Long deptId = Long.valueOf(deptIdObj.toString());
        String pIds = body.get("pIds") != null ? body.get("pIds").toString() : "";
        departmentService.assignPermission(deptId, pIds);
        return Result.MODIFY_SUCCESS;
    }

    @GetMapping("/permissions")
    public Result<List<Long>> getDeptPermissions(Long deptId) {
        return Result.success(departmentService.getDeptPermissionIds(deptId));
    }

    private void checkAdmin() {
        List<RoleDTO> roles = roleService.listUserRoles(UserContext.getUserId());
        boolean isAdmin = roles.stream().anyMatch(r -> "admin".equals(r.getRoleName()));
        if (!isAdmin) {
            throw new ServiceException(SecurityConstant.NO_PERMISSION);
        }
    }
}
