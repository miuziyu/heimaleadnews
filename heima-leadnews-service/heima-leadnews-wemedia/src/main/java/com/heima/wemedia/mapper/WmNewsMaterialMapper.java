package com.heima.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface WmNewsMaterialMapper extends BaseMapper<WmNewsMaterial> {
    void saveRelations(@Param("materialIds")List<Integer> materialIds, Integer newsId, @Param("type")Short type);

}
