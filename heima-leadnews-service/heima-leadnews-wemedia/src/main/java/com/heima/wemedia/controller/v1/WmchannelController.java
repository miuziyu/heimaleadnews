package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.AdChannelDto;
import com.heima.model.wemedia.dtos.ChannelDto;
import com.heima.wemedia.service.WmChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/channel")
public class WmchannelController {
    @Autowired
    private WmChannelService wmChannelService;
    @GetMapping("/channels")
    public ResponseResult findAll(){
        return wmChannelService.findAll();
    }

    @PostMapping("/save")
    public ResponseResult save(@RequestBody AdChannelDto dto)
    {
        return wmChannelService.save(dto);
    }
    @GetMapping("/del/{id}")
    public ResponseResult del(@PathVariable Integer id)
    {
        return wmChannelService.del(id);
    }
    @PostMapping("/list")
    public ResponseResult findlist(@RequestBody ChannelDto dto)
    {
        return wmChannelService.findlist(dto);
    }
    @PostMapping("/update")
    public ResponseResult upate(@RequestBody AdChannelDto dto)
    {
        return wmChannelService.update(dto);
    }
}
