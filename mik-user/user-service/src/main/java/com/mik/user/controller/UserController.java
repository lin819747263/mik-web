package com.mik.user.controller;

import cn.hutool.core.util.StrUtil;
import com.mik.core.exception.ServiceException;
import com.mik.core.pojo.PageInput;
import com.mik.core.pojo.Result;
import com.mik.core.user.UserInfo;
import com.mik.exception.SecurityConstant;
import com.mik.security.UserContext;
import com.mik.sys.OperationLog;
import com.mik.user.controller.cqe.UserRegisterInput;
import com.mik.user.dto.UserCreateDTO;
import com.mik.user.dto.UserDTO;
import com.mik.user.dto.UserQuery;
import com.mik.user.entity.User;
import com.mik.user.service.UserService;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    private PasswordEncoder encoder;


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
    public Result listByConditionPage(UserQuery query,PageInput pageInput){
        return Result.success(userService.listByConditionPage(query, pageInput));
    }

    @PostMapping("/logout")
    public Result logout(){
        userService.logout();
        return Result.success();
    }

    @PostMapping("/resetPassword")
    public Result resetPassword(Long userId){
        if(userId == 1L || UserContext.getUserId().equals(userId)){
            throw new ServiceException(SecurityConstant.NO_PERMISSION);
        }
        User user = userService.getMapper().selectOneById(userId);
        user.setPassword(encoder.encode("123456"));
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
    public Result register(UserRegisterInput  input){
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


    @GetMapping("/redis")
    public Result listByConditionPage() throws InterruptedException {
        if(!redissonClient.getLock("888").tryLock(3, TimeUnit.SECONDS)){
            throw new ServiceException("获取锁失败");
        }
        try {
            redisTemplate.opsForValue().set("redission", "redission");
            TimeUnit.SECONDS.sleep(10L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            redissonClient.getLock("888").unlock();
        }

        return Result.success();
    }


    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sse() throws InterruptedException {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                for (int i = 0; i < 300; i++) {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .data("SSE MVC - " + i)
                            .id(String.valueOf(i))
                            .name("sse event");
                    emitter.send(event);
                    Thread.sleep(1000);
                }
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });

        return emitter;
    }

}
