package com.mik.user.controller;

import com.mik.core.pojo.PageInput;
import com.mik.core.pojo.Result;
import com.mik.sys.OperationLog;
import com.mik.user.controller.cqe.RoleCreateCommand;
import com.mik.user.controller.cqe.RoleQuery;
import com.mik.user.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Resource
    RoleService roleService;

    @GetMapping("page")
    public Result listPermissionPage(RoleQuery query, PageInput pageInput){
        return Result.success(roleService.listRolePage(query, pageInput));
    }

    @GetMapping("detail")
    public Result getById(Long roleId){
        return Result.success(roleService.getById(roleId));
    }

    @OperationLog(operation = "删除角色")
    @PostMapping("delete")
    public Result del(String ids){
        roleService.deleteRole(ids);
        return Result.success();
    }

    @OperationLog(operation = "创建/编辑角色")
    @PostMapping("create")
    public Result create(RoleCreateCommand command){
        roleService.create(command);
        return Result.success();
    }

    @GetMapping("listUserRoles")
    public Result listUserRoles(Long userId){
        return Result.success(roleService.listUserRoles(userId));
    }

    @GetMapping("listAllRoles")
    public Result listAllRoles(){
        return Result.success(roleService.listAllRoles());
    }

}
