package com.mik.user.controller;

import com.mik.core.pojo.PageInput;
import com.mik.core.pojo.Result;
import com.mik.sys.OperationLog;
import com.mik.user.controller.cqe.PermissionCreateCommand;
import com.mik.user.controller.cqe.PermissionDTO;
import com.mik.user.controller.cqe.PermissionQuery;
import com.mik.user.service.PermissionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Resource
    PermissionService permissionService;


    @GetMapping("page")
    public Result listPermissionPage(PermissionQuery query, PageInput pageInput){
        return Result.success(permissionService.listPermissionPage(query, pageInput));
    }

    @GetMapping("detail")
    public Result getById(Long id){
        return Result.success(permissionService.getById(id));
    }

    @OperationLog(operation = "删除菜单")
    @PostMapping("delete")
    public Result del(String ids){
        Set<Long> set = Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toSet());
        permissionService.delPermission(set);
        return Result.success();
    }

    @OperationLog(operation = "创建/编辑权限")
    @PostMapping("create")
    public Result create(PermissionCreateCommand command){
        permissionService.create(command);
        return Result.success();
    }

    @GetMapping("listUserPermission")
    public Result listUserPermission(Long userId){
        List<PermissionDTO> permissionDTOS = permissionService.listUserPermission(userId);
        return Result.success(permissionDTOS);
    }

    @GetMapping("listAllPermission")
    public Result listAllPermission(){
        List<PermissionDTO> res =  permissionService.listAllPermission();
        return Result.success(res);
    }
}
