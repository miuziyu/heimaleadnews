package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.AdChannelDto;
import com.heima.model.wemedia.dtos.ChannelDto;
import com.heima.model.wemedia.pojos.WmChannel;

public interface WmChannelService extends IService<WmChannel> {
    public ResponseResult findAll();
    public ResponseResult save(AdChannelDto dto);
    public ResponseResult del(Integer id);
    public ResponseResult findlist(ChannelDto dto);
    public ResponseResult update (AdChannelDto dto);
}
