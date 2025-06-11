package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.ChannelDto;
import com.heima.model.wemedia.dtos.WmSensitiveDto;
import com.heima.model.wemedia.dtos.WmsensitivePageReqDto;
import com.heima.wemedia.service.WmSensitiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/sensitive")
public class WmSensitiveController {
    @Autowired
    WmSensitiveService wmSensitiveService;
    @PostMapping("/save")
    public ResponseResult save(@RequestBody WmSensitiveDto dto){
        return wmSensitiveService.save(dto);

    }
    @PostMapping("/list")
    public ResponseResult findlist(@RequestBody WmsensitivePageReqDto dto)
    {
        return wmSensitiveService.findlist(dto);
    }
    @PostMapping("/update")
    public ResponseResult update(@RequestBody WmSensitiveDto dto)
    {
        return wmSensitiveService.updatesensitive(dto);
    }
    @DeleteMapping("/del/{id}")
    public ResponseResult delete(@PathVariable Integer id)
    {
        return wmSensitiveService.deletByid(id);
    }

}
