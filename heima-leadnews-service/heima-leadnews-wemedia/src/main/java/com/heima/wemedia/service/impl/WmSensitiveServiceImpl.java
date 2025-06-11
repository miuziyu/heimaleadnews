package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.ChannelDto;
import com.heima.model.wemedia.dtos.WmSensitiveDto;
import com.heima.model.wemedia.dtos.WmsensitivePageReqDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.service.WmSensitiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@Transactional

public class WmSensitiveServiceImpl extends ServiceImpl<WmSensitiveMapper,WmSensitive> implements WmSensitiveService {
    /**
     * @param dto
     * @return
     */
    @Override
    public ResponseResult save(WmSensitiveDto dto) {
        //参数检验
        if(StringUtils.isBlank(dto.getSensitives())  || dto.getId() == null )
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //拷贝参数
        //判断是否已经存在
        WmSensitive sensitive = getOne(Wrappers.<WmSensitive>lambdaQuery().eq(WmSensitive::getSensitives, dto.getSensitives()));
        if(sensitive != null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST);
        }
        WmSensitive wmSensitive = new WmSensitive();
        Boolean flag = saveSensitive(wmSensitive, dto);
        if(!flag)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }


        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findlist(WmsensitivePageReqDto dto) {
        //参数检验
        if(dto == null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //规范分页
        dto.checkParam();
        //查找频道
        IPage<WmSensitive> page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmSensitive> wrapper = new LambdaQueryWrapper<>();
        //根据名字看是搜索还是列表
        if(StringUtils.isNotBlank(dto.getName()))
        {
            //模糊查询
            wrapper.like(WmSensitive::getSensitives,dto.getName());
        }

        //根据创造时间排序
        wrapper.orderByDesc(WmSensitive::getCreatedTime);
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
    public ResponseResult updatesensitive(WmSensitiveDto dto) {
        if(dto == null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        if(StringUtils.isBlank(dto.getSensitives()))
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        WmSensitive wmSensitive = new WmSensitive();
        Boolean issuccess = saveSensitive(wmSensitive, dto);
        if(!issuccess)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public ResponseResult deletByid(Integer id) {
        if(id == null || id<0)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        boolean issuccess = removeById(id);
        if(!issuccess)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * @param dto
     * @return
     */



    private Boolean saveSensitive(WmSensitive wmSensitive, WmSensitiveDto dto) {
        wmSensitive.setCreatedTime(new Date());
        BeanUtils.copyProperties(dto,wmSensitive);

        return save(wmSensitive);
    }
}
