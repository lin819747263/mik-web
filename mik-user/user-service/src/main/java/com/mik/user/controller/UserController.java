package com.mik.user.controller;

import com.mik.core.exception.ServiceException;
import com.mik.core.pojo.PageInput;
import com.mik.core.pojo.Result;
import com.mik.core.user.UserInfo;
import com.mik.user.dto.UserCreateDTO;
import com.mik.user.dto.UserQuery;
import com.mik.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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


    @PostMapping("/createUser")
    public Result createUser(UserCreateDTO createDTO){

        if(StringUtils.isBlank(createDTO.getMobile()) || StringUtils.isBlank(createDTO.getEmail())){
            throw new ServiceException("手机号或者邮箱不能为空");
        }

        userService.createUser(createDTO);
        return Result.CREATE_SUCCESS;
    }

    @PostMapping("/delUser")
    public Result delUser(Long userId){
        userService.delUser(userId);
        return Result.DELETE_SUCCESS;
    }

    @PostMapping("/changeEnable")
    public Result changeEnable(Long userId, Integer enable){
        userService.changeEnable(userId, enable);
        return Result.MODIFY_SUCCESS;
    }

    @GetMapping("/getUserById")
    public Result getUserById(Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    @GetMapping("/getUserByIdentify")
    public UserInfo getUserByIdentify(String identify) {
        return userService.getUserByIdentify(identify);
    }

    @GetMapping("/listByConditionPage")
    public Result listByConditionPage(UserQuery query,PageInput pageInput){
        return Result.success(userService.listByConditionPage(query, pageInput));
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
