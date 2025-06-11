package com.heima.admin.service.Impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdUserMapper;
import com.heima.admin.service.AdminUserService;
import com.heima.model.admin.dtos.AdUserDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.common.AppJwtUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminUserServiceImpl  extends ServiceImpl<AdUserMapper, AdUser> implements AdminUserService {


    /**
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(AdUserDto dto) {
        //检查参数
        if(dto == null || StringUtils.isBlank(dto.getName()))
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //根据名字检查dto的密码
        String name = dto.getName();
        AdUser adUser = getOne(Wrappers.<AdUser>lambdaQuery().eq(AdUser::getName, name));
        if(adUser == null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
       //比对密码
        String salt = adUser.getSalt();
        String pswd = dto.getPassword();
        pswd = DigestUtils.md5DigestAsHex((pswd+salt).getBytes());
        if(!pswd.equals(adUser.getPassword()))
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }

        //登陆成功：返回jwt
        Map<String,Object> jwt = new HashMap<>();
        jwt.put("token", AppJwtUtil.getToken(adUser.getId().longValue()));
        adUser.setPassword("");
        adUser.setSalt("");
        jwt.put("user",adUser);
        return ResponseResult.okResult(jwt);
    }
}
