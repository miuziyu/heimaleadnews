package com.heima.admin.controller.v1;

import com.heima.admin.service.AdminUserService;
import com.heima.model.admin.dtos.AdUserDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dto.LoginDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class AdminLoginController {
    @Autowired
    AdminUserService adminUserService;
    @PostMapping("/in")
    public ResponseResult login(@RequestBody AdUserDto dto)
    {
        return adminUserService.login(dto);
    }
}
