package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.ChannelDto;
import com.heima.model.wemedia.dtos.WmSensitiveDto;
import com.heima.model.wemedia.dtos.WmsensitivePageReqDto;
import com.heima.model.wemedia.pojos.WmSensitive;

public interface WmSensitiveService extends IService<WmSensitive> {
    public ResponseResult save(WmSensitiveDto dto);
    public ResponseResult findlist(WmsensitivePageReqDto dto);
    public ResponseResult updatesensitive(WmSensitiveDto dto);

    public ResponseResult deletByid(Integer id);


}
