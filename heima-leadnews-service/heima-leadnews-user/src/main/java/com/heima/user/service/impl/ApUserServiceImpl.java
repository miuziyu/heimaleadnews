package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import com.heima.model.user.dto.LoginDto;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 47049
 * @version 1.0
 * @description TODO
 * @date 2024/11/6 14:39
 */
@Service
@Transactional
@Slf4j
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService{
    /**
     * @param dto
     * @return
     * @description 登录功能
     * @author 47049
     * @date 10:01
     */
    @Override
    public ResponseResult login(LoginDto dto) {
        //1.正常登录，用户名和密码
        if(StringUtils.isNotBlank(dto.getPhone())&&StringUtils.isNotBlank((dto.getPassword()))) {
            ApUser apUser = getOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, dto.getPhone()));
            if (apUser == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "用户信息不存在");
            }

            //1.1根据手机号查询用户信息

            //1.2比对密码
            String salt = apUser.getSalt();
            String pswd = dto.getPassword();
            pswd = DigestUtils.md5DigestAsHex((pswd + salt).getBytes());
            //1.3返回数据 jwt
            if (!pswd.equals(apUser.getPassword())) {
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("token", AppJwtUtil.getToken(apUser.getId().longValue()));
            apUser.setSalt("");
            map.put("user", apUser);
            return ResponseResult.okResult(map);
        }
        //2.游客登陆
        else {
            Map<String, Object> map = new HashMap<>();
            map.put("token", AppJwtUtil.getToken(0l));
            return ResponseResult.okResult(map);
        }


    }
}
