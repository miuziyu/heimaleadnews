package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.user.service.ApUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heima.model.user.dto.LoginDto;



/**
 * @author 47049
 * @version 1.0
 * @description TODO
 * @date 2024/11/5 15:20
 */

@RestController
@RequestMapping("/api/v1/login")
@Api(value = "app端用户登录",tags = "ap_user",description = "app用户登录API")
public class ApUserLoginController {
    @Autowired
    private ApUserService apUserService;
    @PostMapping("/login_auth")
    @ApiOperation("用户登录")
    public ResponseResult login(@RequestBody LoginDto Dto){
        return apUserService.login(Dto);
    }
}
