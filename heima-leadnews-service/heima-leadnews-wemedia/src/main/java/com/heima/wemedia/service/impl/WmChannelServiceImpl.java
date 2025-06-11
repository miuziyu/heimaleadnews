package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.AdChannelDto;
import com.heima.model.wemedia.dtos.ChannelDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.service.WmChannelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service
@Transactional
@Slf4j
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper,WmChannel> implements WmChannelService {
    /**
     * @return
     */
    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(list());
    }

    /**
     * @param dto
     * @return
     */
    @Override
    public ResponseResult save(AdChannelDto dto) {
        //数据校验

        if(dto == null || StringUtils.isBlank(dto.getName()))
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"频道名不能为空");
        }
        //唯一性检验
        WmChannel adchannel = getOne(Wrappers.<WmChannel>lambdaQuery().eq(WmChannel::getName, dto.getName()));
        if(adchannel != null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST,"已经存在此频道");
        }


        //数据迁移
        WmChannel adChannel = new WmChannel();
        BeanUtils.copyProperties(dto,adChannel);
        adChannel.setCreatedTime(new Date());


        //数据返回
        save(adChannel);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public ResponseResult del(Integer id) {
        //参数检验
        if(id <= 0)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmChannel adchannel = getOne(Wrappers.<WmChannel>lambdaQuery().eq(WmChannel::getId,id));
        if(adchannel == null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"不存在此频道");
        }
        if(adchannel.getStatus() == true)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.CHANNEL_STATUS_TRUE);
        }
        //删除
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findlist(ChannelDto dto) {
        //参数检验
        if(dto == null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //规范分页
        dto.checkParam();
        //查找频道
        IPage<WmChannel> page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmChannel> wrapper = new LambdaQueryWrapper<>();
        //根据名字看是搜索还是列表
        if(StringUtils.isNotBlank(dto.getName()))
        {
            //模糊查询
            wrapper.like(WmChannel::getName,dto.getName());
        }
        //状态：0是停用，1是启用
        if(dto.getStatus() != null && (dto.getStatus() == false || dto.getStatus() == true))
        {

            wrapper.eq(WmChannel::getStatus,dto.getStatus());
        }
        //根据创造时间排序
        wrapper.orderByDesc(WmChannel::getCreatedTime);
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
    public ResponseResult update(AdChannelDto dto) {
        if(dto == null || StringUtils.isBlank(dto.getName()))
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        if(IsReference(dto) && dto.getStatus() == false)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.CHANNEL_REFERENCED_CANNOT_EDIT);
        }
        WmChannel wmChannel = new WmChannel();
        BeanUtils.copyProperties(dto,wmChannel);
        update(dto);
        return null;
    }

    @Autowired
    private WmNewsMapper wmNewsMapper;
    private boolean IsReference(AdChannelDto dto) {
        boolean flag = false;
        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<WmNews>().eq(WmNews::getChannelId,dto.getId());
        if(wmNewsMapper.selectCount(wrapper)>0)
        {
            flag = true;

        }return flag;
    }




}
