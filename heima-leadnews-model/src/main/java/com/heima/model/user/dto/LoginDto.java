package com.heima.model.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author 47049
 * @version 1.0
 * @description TODO
 * @date 2024/11/6 13:31
 */

@Data
public class LoginDto {
    /**
     * @description 手机
     * @param null
     * @return
     * @author 47049
     * @date  13:32
    */
    @ApiModelProperty(value="手机号",required = true)
    private String phone;
    /**
     * @description 密码
     * @param null
     * @return
     * @author 47049
     * @date  13:32
    */
    @ApiModelProperty(value="密码",required = true)
    private String password;
}
