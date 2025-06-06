package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.dto.LoginDto;

/**
 * @author 47049
 * @version 1.0
 * @description TODO
 * @date 2024/11/6 13:52
 */
public interface ApUserService extends IService<ApUser> {
    /**
     * @description 登录功能
     * @param dto
     * @return
     * @author 47049
     * @date  10:01
    */
    public ResponseResult login(LoginDto dto);
}
