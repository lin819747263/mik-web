package com.mik.user.controller;

import com.mik.core.exception.ServiceException;
import com.mik.core.pojo.PageInput;
import com.mik.core.pojo.Result;
import com.mik.core.user.UserInfo;
import com.mik.exception.SecurityConstant;
import com.mik.security.UserContext;
import com.mik.sys.OperationLog;
import com.mik.user.controller.cqe.UserRegisterInput;
import com.mik.user.dto.UserCreateDTO;
import com.mik.user.dto.UserQuery;
import com.mik.user.entity.User;
import com.mik.user.service.UserService;
import com.mik.user.service.RoleService;
import com.mik.user.controller.cqe.RoleDTO;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private com.mik.dept.mapper.DepartmentMapper departmentMapper;


    @OperationLog(operation = "创建/编辑用户")
    @PostMapping("/createUser")
    public Result createUser(UserCreateDTO createDTO){
        userService.createUser(createDTO);
        return Result.CREATE_SUCCESS;
    }

    @OperationLog(operation = "删除用户")
    @PostMapping("/delUser")
    public Result delUser(Long userId){
        userService.delUser(userId);
        return Result.DELETE_SUCCESS;
    }

    @OperationLog(operation = "启用/禁用用户")
    @PostMapping("/changeEnable")
    public Result changeEnable(Long userId, Integer enable){
        userService.changeEnable(userId, enable);
        if(enable == 0){
            userService.logout();
        }
        return Result.MODIFY_SUCCESS;
    }

    @GetMapping("/getUserById")
    public Result getUserById(Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    @GetMapping("/getUserInfo")
    public Result getUserInfo() {
        return Result.success(userService.getUserById(UserContext.getUserId()));
    }

    @GetMapping("/getUserByIdentify")
    public UserInfo getUserByIdentify(String identify) {
        return userService.getUserByIdentify(identify);
    }

    @GetMapping("/listByConditionPage")
    public Result listByConditionPage(UserQuery query, PageInput pageInput){
        return Result.success(userService.listByConditionPage(query, pageInput));
    }

    /**
     * 简单用户列表（不分页，供选择器使用）
     */
    @GetMapping("/simpleList")
    public Result simpleList() {
        List<User> users = userService.getMapper().selectListByQuery(
                QueryWrapper.create().select().from("user").limit(500));
        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", u.getUserId());
            map.put("username", u.getUsername());
            map.put("nickname", u.getNickname());
            map.put("deptId", u.getDeptId());
            list.add(map);
        }
        return Result.success(list);
    }

    /**
     * 按部门查询用户列表（含子部门）
     */
    @GetMapping("/listByDept")
    public Result listByDept(Long deptId) {
        List<User> users;
        if (deptId == null) {
            users = userService.getMapper().selectListByQuery(
                    QueryWrapper.create().select().from("user").limit(500));
        } else {
            List<Long> deptIds = new ArrayList<>();
            deptIds.add(deptId);
            collectChildDeptIds(deptId, deptIds);
            QueryWrapper wrapper = QueryWrapper.create()
                    .select().from("user")
                    .where(new QueryColumn("dept_id").in(deptIds));
            users = userService.getMapper().selectListByQuery(wrapper);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", u.getUserId());
            map.put("username", u.getUsername());
            map.put("nickname", u.getNickname());
            map.put("deptId", u.getDeptId());
            list.add(map);
        }
        return Result.success(list);
    }

    /**
     * 递归收集子部门ID（一次性查询所有部门，内存中递归）
     */
    private void collectChildDeptIds(Long parentId, List<Long> ids) {
        List<com.mik.dept.entity.Department> allDepts = departmentMapper.selectListByQuery(
                QueryWrapper.create().select("dept_id", "parent_id").from("department"));
        Map<Long, List<Long>> parentChildMap = allDepts.stream()
                .collect(Collectors.groupingBy(
                        com.mik.dept.entity.Department::getParentId,
                        Collectors.mapping(com.mik.dept.entity.Department::getDeptId, Collectors.toList())));
        collectChildIdsRecursive(parentId, ids, parentChildMap);
    }

    private void collectChildIdsRecursive(Long parentId, List<Long> ids, Map<Long, List<Long>> parentChildMap) {
        List<Long> children = parentChildMap.getOrDefault(parentId, Collections.emptyList());
        for (Long childId : children) {
            ids.add(childId);
            collectChildIdsRecursive(childId, ids, parentChildMap);
        }
    }

    @PostMapping("/logout")
    public Result logout(){
        userService.logout();
        return Result.success();
    }

    @OperationLog(operation = "重置密码")
    @PostMapping("/resetPassword")
    public Result resetPassword(@RequestBody Map<String, Object> body){
        Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
        if(userId == null){
            throw new ServiceException("用户ID不能为空");
        }
        if(userId == 1L || UserContext.getUserId().equals(userId)){
            throw new ServiceException(SecurityConstant.NO_PERMISSION);
        }
        // 校验调用者是否为管理员
        List<RoleDTO> roles = roleService.listUserRoles(UserContext.getUserId());
        boolean isAdmin = roles.stream().anyMatch(r -> "admin".equals(r.getRoleName()));
        if (!isAdmin) {
            throw new ServiceException(SecurityConstant.NO_PERMISSION);
        }
        User user = userService.getMapper().selectOneById(userId);
        user.setPassword(encoder.encode("sy123456#"));
        userService.saveOrUpdate(user);
        return Result.success();
    }

    @OperationLog(operation = "修改密码", paramRecord = false)
    @PostMapping("/changePassword")
    public Result changePassword(String oldPassword, String newPassword){
        User user = userService.getMapper().selectOneById(UserContext.getUserId());
        if(!encoder.matches(oldPassword, user.getPassword())){
            throw new ServiceException("旧密码错误");
        }
        user.setPassword(encoder.encode(newPassword));
        userService.saveOrUpdate(user);
        userService.logout();
        return Result.success();
    }

    @OperationLog(operation = "用户注册")
    @PostMapping("/register")
    public Result register(UserRegisterInput input){
        User user = userService.getMapper().selectOneByCondition(
                QueryCondition.create(new QueryColumn("mobile"), "=", input.getMobile()));
        if(user != null){
            throw new ServiceException("手机号已存在");
        }
        User createDTO = new User();
        createDTO.setMobile(input.getMobile());
        createDTO.setPassword(encoder.encode(input.getPassword()));
        userService.saveOrUpdate(createDTO);
        return Result.success();
    }

}
