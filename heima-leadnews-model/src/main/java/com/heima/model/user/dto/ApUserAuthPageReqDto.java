package com.heima.model.user.dto;

import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;

@Data
public class ApUserAuthPageReqDto extends PageRequestDto {
    private Integer id;
    private String msg;
    private Integer status;

}
