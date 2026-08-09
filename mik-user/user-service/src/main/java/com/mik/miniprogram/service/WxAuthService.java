package com.mik.miniprogram.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mik.core.exception.ServiceException;
import com.mik.core.user.UserInfo;
import com.mik.miniprogram.dto.WxLoginDTO;
import com.mik.miniprogram.dto.WxLoginVO;
import com.mik.miniprogram.entity.WxUser;
import com.mik.miniprogram.mapper.WxUserMapper;
import com.mik.user.controller.cqe.RoleDTO;
import com.mik.user.entity.User;
import com.mik.user.mapper.UserMapper;
import com.mik.user.service.RoleService;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class WxAuthService {

    @Autowired
    private WxUserMapper wxUserMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${wx.appid:}")
    private String appid;

    @Value("${wx.secret:}")
    private String secret;

    @Value("${jwt.secret:mik-default-secret-key-at-least-32bytes}")
    private String jwtSecret;

    private static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    /**
     * 小程序登录
     */
    @Transactional
    public WxLoginVO login(WxLoginDTO dto) {
        // 1. 调用微信接口获取openid
        JSONObject wxResult = getWxSession(dto.getCode());
        if (wxResult == null || wxResult.getString("openid") == null) {
            throw new ServiceException("微信登录失败");
        }

        String openid = wxResult.getString("openid");
        String unionid = wxResult.getString("unionid");
        String sessionKey = wxResult.getString("session_key");

        // 2. 查询或创建微信用户
        WxUser wxUser = wxUserMapper.selectOneByCondition(
                QueryCondition.create(new QueryColumn("openid"), "=", openid));

        if (wxUser == null) {
            // 新用户，创建微信用户记录
            wxUser = new WxUser();
            wxUser.setOpenid(openid);
            wxUser.setUnionid(unionid);
            wxUser.setCreatedAt(LocalDateTime.now());
            wxUser.setUpdatedAt(LocalDateTime.now());

            // 创建系统用户（使用完整 openid 避免碰撞）
            User user = new User();
            user.setUsername("wx_" + openid);
            user.setMobile("");
            user.setEmail("");
            user.setPassword("");
            user.setEnable(1);
            user.setCreateTime(new Date());
            userMapper.insert(user);

            wxUser.setUserId(user.getUserId());
            wxUserMapper.insert(wxUser);
        } else {
            // 老用户，更新时间
            wxUser.setUpdatedAt(LocalDateTime.now());
            if (unionid != null) {
                wxUser.setUnionid(unionid);
            }
            wxUserMapper.update(wxUser);
        }

        // 3. 生成JWT token
        String token = sign(wxUser.getOpenid());

        // 4. 将用户信息存储到Redis中（兼容现有的认证逻辑）
        User user = userMapper.selectOneById(wxUser.getUserId());
        if (user != null) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(user.getUserId());
            userInfo.setUsername(user.getUsername());
            userInfo.setNickname(wxUser.getNickname());
            userInfo.setMobile(user.getMobile());
            userInfo.setEmail(user.getEmail());
            userInfo.setAvatar(wxUser.getAvatar());
            userInfo.setEnable(user.getEnable());

            String hash = UUID.randomUUID().toString().replace("-", "");
            String redisKey = StrUtil.format("Auth:{}:{}", wxUser.getOpenid(), hash);
            String infoKey = StrUtil.format("info:{}", wxUser.getOpenid());

            // 更新token，添加hash信息
            token = signWithHash(wxUser.getOpenid(), hash);

            redisTemplate.opsForValue().set(redisKey, token, 7, TimeUnit.DAYS);
            redisTemplate.opsForValue().set(infoKey, JSON.toJSONString(userInfo), 7, TimeUnit.DAYS);
        }

        // 5. 获取用户角色
        List<RoleDTO> userRoles = roleService.listUserRoles(wxUser.getUserId());
        List<String> roleNames = userRoles.stream()
                .map(RoleDTO::getRoleName)
                .collect(Collectors.toList());

        // 判断主要角色：admin > operator > citizen
        String primaryRole = "citizen"; // 默认为市民
        if (roleNames.contains("admin")) {
            primaryRole = "admin";
        } else if (roleNames.contains("operator")) {
            primaryRole = "operator";
        }

        // 6. 返回结果
        WxLoginVO vo = new WxLoginVO();
        vo.setToken(token);
        vo.setOpenid(openid);
        vo.setUnionid(unionid);
        vo.setRole(primaryRole);
        vo.setRoleNames(roleNames);
        vo.setNickname(wxUser.getNickname());
        vo.setAvatar(wxUser.getAvatar());

        return vo;
    }

    /**
     * 调用微信接口获取session信息
     */
    private JSONObject getWxSession(String code) {
        String url = String.format(WX_LOGIN_URL, appid, secret, code);
        try {
            String result = restTemplate.getForObject(url, String.class);
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            throw new ServiceException("调用微信接口失败: " + e.getMessage());
        }
    }

    /**
     * 生成JWT token
     */
    private String sign(String openid) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", openid);
        claims.put("type", "miniprogram");
        claims.put("iat", new Date());

        String secretKey = Base64.getEncoder().encodeToString(jwtSecret.getBytes(StandardCharsets.UTF_8));

        long expirationTime = 7 * 24 * 60 * 60 * 1000;
        Date expirationDate = new Date(System.currentTimeMillis() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * 生成带hash的JWT token（兼容现有认证逻辑）
     */
    private String signWithHash(String openid, String hash) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", openid);
        claims.put("hash", hash);
        claims.put("type", "miniprogram");
        claims.put("iat", new Date());

        String secretKey = Base64.getEncoder().encodeToString(jwtSecret.getBytes(StandardCharsets.UTF_8));

        long expirationTime = 7 * 24 * 60 * 60 * 1000;
        Date expirationDate = new Date(System.currentTimeMillis() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * 根据openid获取微信用户
     */
    public WxUser getWxUserByOpenid(String openid) {
        return wxUserMapper.selectOneByCondition(
                QueryCondition.create(new QueryColumn("openid"), "=", openid));
    }

    /**
     * 根据openid获取系统用户信息
     */
    public User getUserByOpenid(String openid) {
        WxUser wxUser = getWxUserByOpenid(openid);
        if (wxUser == null || wxUser.getUserId() == null) {
            return null;
        }
        return userMapper.selectOneById(wxUser.getUserId());
    }
}
