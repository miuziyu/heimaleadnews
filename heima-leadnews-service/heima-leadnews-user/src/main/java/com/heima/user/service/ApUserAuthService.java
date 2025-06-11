package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dto.ApUserAuthPageReqDto;
import com.heima.model.user.pojos.ApUserRealname;


public interface ApUserAuthService extends IService<ApUserRealname> {

   public ResponseResult findlist(ApUserAuthPageReqDto dto);

   ResponseResult authfail(ApUserAuthPageReqDto dto);

   ResponseResult authpass(ApUserAuthPageReqDto dto);
}
