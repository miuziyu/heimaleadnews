package com.heima.user.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dto.ApUserAuthPageReqDto;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.user.mapper.ApUserRealnameMapper;
import com.heima.user.service.ApUserAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@Transactional
public class ApUserAuthServiceImpl extends ServiceImpl<ApUserRealnameMapper, ApUserRealname> implements ApUserAuthService {
    /**
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findlist(ApUserAuthPageReqDto dto) {
        //参数检验
        if(dto == null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //规范分页
        dto.checkParam();
        //查找
        IPage<ApUserRealname> page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<ApUserRealname> wrapper = new LambdaQueryWrapper<>();



        //根据创造时间排序
        wrapper.orderByDesc(ApUserRealname::getCreatedTime);
        //分页查询
        page = page(page, wrapper);
        ResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)page.getTotal());
        responseResult.setData(page.getRecords());



        return responseResult;
    }

    /**
     * @param dto
     * @return
     */
    @Override
    public ResponseResult authfail(ApUserAuthPageReqDto dto) {
        if(dto == null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        if(dto.getStatus() != 1 || dto.getStatus() != 9 || dto.getStatus() != 2)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUserRealname apUserRealname = getById(dto.getId());
        if(apUserRealname == null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        apUserRealname.setUpdatedTime(new Date());
        apUserRealname.setReason(dto.getMsg());
        apUserRealname.setStatus((short) 2);
        updateById(apUserRealname);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);




    }

    /**
     * @param dto
     * @return
     */
    @Override
    public ResponseResult authpass(ApUserAuthPageReqDto dto) {
        if(dto == null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        if(dto.getStatus() != 1 || dto.getStatus() != 9 || dto.getStatus() != 2)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUserRealname apUserRealname = getById(dto.getId());
        if(apUserRealname == null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        apUserRealname.setUpdatedTime(new Date());
        apUserRealname.setReason(dto.getMsg());
        apUserRealname.setStatus((short) 9);
        updateById(apUserRealname);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

    }
}
