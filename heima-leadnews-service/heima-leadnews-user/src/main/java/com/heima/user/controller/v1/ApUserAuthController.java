package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dto.ApUserAuthPageReqDto;
import com.heima.user.service.ApUserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class ApUserAuthController {
    @Autowired
    private ApUserAuthService apUserAuthService;
    @PostMapping("/list")
    public ResponseResult findlist(@RequestBody ApUserAuthPageReqDto dto)
    {
        return apUserAuthService.findlist(dto);
    }
    @PostMapping("/authFail")
    public ResponseResult authfail(@RequestBody ApUserAuthPageReqDto dto)
    {
        return apUserAuthService.authfail(dto);
    }

    @PostMapping("/authPass")

    public ResponseResult authpass(@RequestBody ApUserAuthPageReqDto dto)
    {
        return apUserAuthService.authpass(dto);
    }
}
