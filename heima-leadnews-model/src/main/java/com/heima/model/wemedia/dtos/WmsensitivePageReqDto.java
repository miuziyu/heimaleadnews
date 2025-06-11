package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;

@Data
public class WmsensitivePageReqDto extends PageRequestDto {
    private String name;
}
