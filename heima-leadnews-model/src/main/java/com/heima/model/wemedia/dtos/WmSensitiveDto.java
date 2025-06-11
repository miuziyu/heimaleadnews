package com.heima.model.wemedia.dtos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class WmSensitiveDto {
    /**
     * 主键
     */

    private Integer id;

    /**
     * 敏感词
     */

    private String sensitives;

    /**
     * 创建时间
     */

    private Date createdTime;
}
