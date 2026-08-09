package com.mik.miniprogram.controller;

import com.mik.core.pojo.Result;
import com.mik.miniprogram.dto.WxLoginDTO;
import com.mik.miniprogram.dto.WxLoginVO;
import com.mik.miniprogram.service.WxAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/auth")
public class WxAuthController {

    @Autowired
    private WxAuthService wxAuthService;

    /**
     * 小程序登录
     * POST /auth/login
     */
    @PostMapping("/login")
    public Result<WxLoginVO> login(@RequestBody WxLoginDTO dto) {
        return Result.success(wxAuthService.login(dto));
    }
}
