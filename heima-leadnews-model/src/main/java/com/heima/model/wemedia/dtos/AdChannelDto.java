package com.heima.model.wemedia.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 频道数据传输对象（DTO）
 * 用于接口参数和返回值，与前端字段一致
 */
@Data
public class AdChannelDto {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 频道名称
     */
    private String name;

    /**
     * 状态（true=启用，false=禁用）
     */
    private Boolean status;

    /**
     * 频道描述
     */
    private String description;

    /**
     * 是否为默认频道（true=默认，false=非默认）
     */
    private Boolean isDefault;

    /**
     * 排序
     */
    private Integer ord;

    /**
     * 创建时间
     */
    private Date createdTime;
}